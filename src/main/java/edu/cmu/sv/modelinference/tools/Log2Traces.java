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
package edu.cmu.sv.modelinference.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ModelInferer;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;
import edu.cmu.sv.modelinference.generators.parser.reader.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.parser.st.Coord2d;
import edu.cmu.sv.modelinference.generators.parser.st.GridState;
import edu.cmu.sv.modelinference.generators.parser.st.STEntry;
import edu.cmu.sv.modelinference.generators.parser.st.STParser;
import edu.cmu.sv.modelinference.generators.parser.st.util.GridDimensionsFinder;
import edu.cmu.sv.modelinference.generators.parser.st.util.GridDimensionsFinder.Dimensions;
import edu.cmu.sv.modelinference.generators.parser.st.STGridStateFactory;
import edu.cmu.sv.modelinference.generators.parser.st.STModelInferer;
import edu.cmu.sv.modelinference.generators.trace.TimedTrace;
import edu.cmu.sv.modelinference.generators.trace.TraceGenerator;
import edu.cmu.sv.modelinference.tools.charting.STValueTracker.FIELD;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
import edu.cmu.sv.modelinference.tools.cmdutil.Util.GridPartitions;

/**
 * @author Kasper Luckow
 */
public class Log2Traces {
  private static final String HELP_ARG = "help";
  private static final String INPUT_ARG = "i";
  private static final String INPUT_TYPE_ARG = "t";
  private static final String ADDITIONAL_INPUT_TYPE_ARG = "a";
  
  private static final Logger logger = LoggerFactory.getLogger(Log2Traces.class.getName());
      
  
  public static void main(String[] args) throws IOException, ParseException {    
    Options options = createCmdOptions();
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch(ParseException exp) {
      printHelpAndExit(options);
    }
    
    if(cmd.hasOption(HELP_ARG)) {
      printHelpAndExit(options);
    }
    
    String logFilePath = null;
    if(cmd.hasOption(INPUT_ARG)) {
      logFilePath = cmd.getOptionValue(INPUT_ARG);
    } else {
      printHelpAndExit(options);
    }
    
    //use enums instead
    String logType = "";
    
    //When options are required (required() called in option builder), do
    //we need to check hasValue, or will the parser simply throw an exception
    //if the option is not provided?
    if(cmd.hasOption(INPUT_TYPE_ARG)) {
      logType = cmd.getOptionValue(INPUT_TYPE_ARG);
    } else
      printHelpAndExit(options);
    
    long start = System.currentTimeMillis();
    
    //Expand this to be more pluggable. ugly
    switch(logType.toLowerCase()) {
    case "st":
      GridPartitions parts = null;
      if(cmd.hasOption(ADDITIONAL_INPUT_TYPE_ARG)) {
        String partStr = cmd.getOptionValue(ADDITIONAL_INPUT_TYPE_ARG).trim();
        parts = Util.extractGridPartitions(partStr);
      } else
        parts = GridPartitions.createDefault();
      
      LogEntryFilter<STEntry> filter = LogEntryFilter.<STEntry>EVERYTHING();
      
      LogReader<STEntry> reader = new SequentialLogReader<>(new STParser(), filter);
      GridDimensionsFinder d = new GridDimensionsFinder();
      Dimensions dim = d.start(logFilePath);
      STGridStateFactory stateGen = new STGridStateFactory(
          new Coord2d(dim.minX, dim.minY), 
          new Coord2d(dim.maxX, dim.maxY), 
          parts.horiz, 
          parts.vert);
      
      TraceGenerator<STEntry, GridState> traceGenerator = new TraceGenerator<>(reader, stateGen);
      Collection<TimedTrace<GridState>> traces = traceGenerator.computeTraces(logFilePath);
      PrintWriter pw = new PrintWriter(new File("./bla.txt"));
      for(TimedTrace<GridState> trace : traces) {

        pw.println(trace.toString());
        //System.out.println(trace.toString());
      }
      pw.close();
      
      break;
    case "sierra":
    case "autoresolver":
    case "rp":
      default:
        logger.error("Unsupported input type");
        printHelpAndExit(options);
    }
    
    logger.info("Done generating traces");
    logger.info("Took: " + (System.currentTimeMillis() - start));
  }
  
  private static void printHelpAndExit(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(Log2Traces.class.getName(), options);
    System.exit(0);
  }
  
  public static Options createCmdOptions() {
    Options options = new Options();
    
    Option help = new Option(HELP_ARG, "print this message");

    Option inputType = Option.builder(INPUT_TYPE_ARG).argName("st | autoresolver")
        .hasArg()
        .desc("Specify log type")
        .required()
        .build();
    
    Option addOpts = Option.builder(ADDITIONAL_INPUT_TYPE_ARG).argName("Additional options").hasArg()
        .desc("Additional input type options").build();
    
    Option input = Option.builder(INPUT_ARG).argName("file")
                                .hasArg()
                                .desc("Specify input file.")
                                .required()
                                .build();


    options.addOption(inputType);
    options.addOption(addOpts);
    options.addOption(help);
    options.addOption(input);
    return options;
  }
}
