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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.mc.ModelAdapter;
import edu.cmu.sv.modelinference.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
import edu.cmu.sv.modelinference.tools.pp.Format;
import edu.cmu.sv.modelinference.tools.pp.PrettyPrinter;

/**
 * @author Kasper Luckow
 */
public class Log2Model implements LogHandler<Void> {

  private static final String RUN_MODEL_CHECKER_ARG = "runmc";
  private static final String HELP_ARG = "help";
  private static final String OUTPUT_ARG = "o";
  private static final String MODEL_CHECKER_ARG = "m";
  private static final String PROPERTIES_ARG = "p";
  private static final String TO_DOT_ARG = "v";

  private static final Logger logger = LoggerFactory.getLogger(Log2Model.class.getName());

  private static Log2Model instance = null;
  
  public static Log2Model getInstance() {
    if(instance == null) {
      instance = new Log2Model();
    }
    return instance;
  }

  private final Options cmdOpts;
  
  private Log2Model() {
    this.cmdOpts = this.createCmdOptions();
  }
  
  private static Set<LogHandler<ModelCheckerAdapter<?, ?>>> modelCheckerHandlers = new HashSet<>();
  private static Set<LogHandler<Model<?>>> intermediateModelHandlers = new HashSet<>();
  
  static {
    //Add supported intermediate model generators
    intermediateModelHandlers.add(STLog2ModelHandler.getInstance());
    
    //Add supported model checkers
    modelCheckerHandlers.add(PrismLog2ModelHandler.getInstance());
    modelCheckerHandlers.add(UppaalLog2ModelHandler.getInstance());
  }

  private Options createCmdOptions() {
    Options options = new Options();

    Option help = new Option(HELP_ARG, "print this message");
    Option runModelChecker = new Option(RUN_MODEL_CHECKER_ARG, false, "Run model checker");
    Option properties = new Option(PROPERTIES_ARG, false, "Run model checker");

    Option modelChecker = Option.builder(MODEL_CHECKER_ARG).
        argName(Util.getSupportedHandlersString(modelCheckerHandlers)).
        hasArg().
        desc("Specify output path of resulting models").required().build();
    
    Option outputPath = Option.builder(OUTPUT_ARG).argName("path").hasArg()
        .desc("Specify output path of resulting models").required().build();

    Option toDot = Option.builder(TO_DOT_ARG).desc("Output model to DOT").build();

    //options.addOption(runModelChecker);
    //options.addOption(properties);
    options.addOption(modelChecker);
    options.addOption(outputPath);
    options.addOption(toDot);
    options.addOption(help);
    return options;
  }

  
  @Override
  public String getHandlerName() {
    return "model";
  }
  
  @Override
  public Void process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    Stopwatch sw = Stopwatch.createStarted();
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch (ParseException e) {
      throw new LogProcessingException(e);
    }
    
    boolean runModelChecker = cmd.hasOption(RUN_MODEL_CHECKER_ARG);
    if(runModelChecker && !cmd.hasOption(PROPERTIES_ARG)) {
      String err = "Properties needed when executing model checker directly.";
      logger.error(err);
      throw new LogProcessingException(err + " Add argument " + PROPERTIES_ARG);
    }
    
    LogHandler<Model<?>> intermediateModelHandler = null;
    boolean found = false;
    for(LogHandler<Model<?>> lh : intermediateModelHandlers) {
      if(lh.getHandlerName().equals(logType)) {
        intermediateModelHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<Model<?>>> logIter = intermediateModelHandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find intermediate model generator for log type " + logType);
      throw new LogProcessingException("Supported log handlers: " + sb.toString());
    }
    logger.info("Using intemediate model generator for logtype: " + intermediateModelHandler.getHandlerName());
    
    String modelCheckerHandlerOpt = cmd.getOptionValue(MODEL_CHECKER_ARG);
    
    LogHandler<ModelCheckerAdapter<?, ?>> modelCheckerHandler = null;
    found = false;
    for(LogHandler<ModelCheckerAdapter<?, ?>> lh : modelCheckerHandlers) {
      if(lh.getHandlerName().equals(modelCheckerHandlerOpt)) {
        modelCheckerHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<ModelCheckerAdapter<?, ?>>> logIter = modelCheckerHandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find handler for model checker type " + logType);
      throw new LogProcessingException("Supported model checkers: " + sb.toString());
    }
    logger.info("Using model checker for: " + modelCheckerHandler.getHandlerName());
    
    String outputPath = cmd.getOptionValue(OUTPUT_ARG);
    logger.info("Outputting to " + outputPath);
    
    logger.info("Generating intermediate model");
    Model<?> irmodel = intermediateModelHandler.process(logFile, logType, cmd.getArgs());
    logger.info("Generating intermediate model took: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
    
    ModelCheckerAdapter<?, ?> modelChecker = modelCheckerHandler.process(logFile, logType, cmd.getArgs());
    logger.info("Generating model checker took: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
//    
//    if(runModelChecker) {
//      throw new LogProcessingException("Running model checker is not supported yet.");
//      //String props = cmd.getOptionValue(PROPERTIES_ARG);
//      //modelChecker.executeModelChecker(irModel, properties);
//    } else {
//      ModelAdapter<?> model = modelChecker.generateModel(irmodel);
//      logger.info("Generating final model took: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
//      logger.info("Saving model to " + outputPath);
//      try {
//        model.writeModelToFile(outputPath);
//      } catch (IOException e) {
//        throw new LogProcessingException(e);
//      }
//    }
    
    if(cmd.hasOption(TO_DOT_ARG)) {
      PrettyPrinter p = new PrettyPrinter(irmodel);
      p.printModel(outputPath, Format.PDF);
      logger.info("Prettyprinting took: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }
    
    logger.info("Total processing time: " + sw.elapsed(TimeUnit.SECONDS) + "s");
    
    return null;
  }
}
