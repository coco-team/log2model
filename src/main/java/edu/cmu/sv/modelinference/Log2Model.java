/**
 * Copyright 2016 Carnegie Mellon University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cmu.sv.modelinference;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.generators.IRModelGenerator;
import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ModelInferer;
import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;
import edu.cmu.sv.modelinference.generators.parser.reader.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.parser.st.GridState;
import edu.cmu.sv.modelinference.generators.parser.st.STModelInferer;
import edu.cmu.sv.modelinference.mc.ModelAdapter;
import edu.cmu.sv.modelinference.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.mc.prism.PrismModelChecker;
import edu.cmu.sv.modelinference.mc.uppaal.UppaalModelChecker;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
import edu.cmu.sv.modelinference.tools.cmdutil.Util.GridPartitions;
import edu.cmu.sv.modelinference.tools.pp.Format;
import edu.cmu.sv.modelinference.tools.pp.PrettyPrinter;

/**
 * @author Kasper Luckow
 */
public class Log2Model {
  private static final String RUN_MODEL_CHECKER_ARG = "runmc";
  private static final String MODEL_CHECKER_ARG = "format";
  private static final String HELP_ARG = "help";
  private static final String OUTPUT_ARG = "o";
  private static final String INPUT_ARG = "i";
  private static final String INPUT_TYPE_ARG = "t";
  private static final String ADDITIONAL_INPUT_TYPE_ARG = "a";
  private static final String TO_DOT_ARG = "v";

  private static final Logger logger = LoggerFactory.getLogger(Log2Model.class.getName());

  public static void main(String[] args) throws ParseException, IOException {
    Options options = createCmdOptions();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException exp) {
      printHelpAndExit(options);
    }

    if (cmd.hasOption(HELP_ARG)) {
      printHelpAndExit(options);
    }

    String logFilePath = null;
    if (cmd.hasOption(INPUT_ARG)) {
      logFilePath = cmd.getOptionValue(INPUT_ARG);
    } else {
      printHelpAndExit(options);
    }

    String outputPath = "";
    if (cmd.hasOption(OUTPUT_ARG)) {
      outputPath = cmd.getOptionValue(OUTPUT_ARG);
    } else
      printHelpAndExit(options);

    // use enums instead
    String logType = "";

    // When options are required (required() called in option builder), do
    // we need to check hasValue, or will the parser simply throw an exception
    // if the option is not provided?
    if(cmd.hasOption(INPUT_TYPE_ARG)) {
      logType = cmd.getOptionValue(INPUT_TYPE_ARG);
    } else
      printHelpAndExit(options);

    long startTime = System.currentTimeMillis();
    
    Model<?> model = null;
    
    //First, generate IR model
    switch(logType.toLowerCase()) {
    case "st":
      ModelInferer<GridState> modelInferer = null;
      if(cmd.hasOption(ADDITIONAL_INPUT_TYPE_ARG)) {
        String partStr = cmd.getOptionValue(ADDITIONAL_INPUT_TYPE_ARG).trim();
        GridPartitions parts = Util.extractGridPartitions(partStr);
        modelInferer = new STModelInferer(parts.horiz, parts.vert);
      } else
        modelInferer = new STModelInferer(STModelInferer.DEF_PARTITIONS, STModelInferer.DEF_PARTITIONS);
      
      model = modelInferer.generateModel(logFilePath);

      
      break;
    case "sierra":
    case "autoresolver":
    case "rp":
      default:
        printHelpAndExit(options);
    }
    
    long irModelTimeStamp = System.currentTimeMillis();
    logger.info("Generating IR model took: " + (irModelTimeStamp - startTime));
    logger.info("Number of states in generated model: " + model.getStateSpaceSize());
    
    //Second, convert IR model to a concrete model

    if(cmd.hasOption(RUN_MODEL_CHECKER_ARG)) {
      logger.warn("Running model checker directly is not fully integrated");
    }
    
    ModelCheckerAdapter<?, ?> mc = null;
    String modelChecker = cmd.getOptionValue(MODEL_CHECKER_ARG);
    switch(modelChecker.toLowerCase()) {
    case "prism":
      mc = new PrismModelChecker();
      break;
    case "uppaal":
      mc = new UppaalModelChecker();
      break;
      default:
        printHelpAndExit(options);
    }
    
    long modelGenTimeStamp = System.currentTimeMillis();
    logger.info("Time taken for IR model and model conversion: " + (modelGenTimeStamp - irModelTimeStamp));
    
    //ModelAdapter<?> s = mc.generateModel(model);
    //s.writeModelToFile(outputPath);

    if(cmd.hasOption(TO_DOT_ARG)) {
      PrettyPrinter p = new PrettyPrinter(model);
      p.printModel(outputPath, Format.PDF);
      logger.info("Prettyprinting: " + (System.currentTimeMillis() - modelGenTimeStamp));
    }

    logger.info("Total processing time : " + (System.currentTimeMillis() - startTime));
  }

  private static void printHelpAndExit(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(Log2Model.class.getName(), options);
    System.exit(0);
  }

  private static Options createCmdOptions() {
    Options options = new Options();

    Option help = new Option(HELP_ARG, "print this message");
    Option runModelChecker = new Option(RUN_MODEL_CHECKER_ARG, false, "Run model checker");

    Option modelChecker = Option.builder(MODEL_CHECKER_ARG).argName("prism | uppaal").hasArg()
        .desc("Specify output format").required().build();

    Option inputType = Option.builder(INPUT_TYPE_ARG).argName("st | autoresolver").hasArg()
        .desc("Specify log type").required().build();
   
    Option addInputTypeOpts = Option.builder(ADDITIONAL_INPUT_TYPE_ARG).argName("additional options").hasArg()
        .desc("Additional input type options").build();

    Option outputPath = Option.builder(OUTPUT_ARG).argName("path").hasArg()
        .desc("Specify output path of resulting models").required().build();

    Option input = Option.builder(INPUT_ARG).argName("file").hasArg().desc("Specify input file.").required().build();
    Option toDot = Option.builder(TO_DOT_ARG).desc("Output model to DOT").build();

    options.addOption(inputType);
    options.addOption(addInputTypeOpts);
    options.addOption(runModelChecker);
    options.addOption(modelChecker);
    options.addOption(outputPath);
    options.addOption(toDot);
    options.addOption(help);
    options.addOption(input);
    return options;
  }
}
