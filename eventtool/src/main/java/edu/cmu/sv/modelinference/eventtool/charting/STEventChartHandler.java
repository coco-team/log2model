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
package edu.cmu.sv.modelinference.eventtool.charting;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.common.Util;
import edu.cmu.sv.modelinference.common.api.LogHandler;
import edu.cmu.sv.modelinference.common.formats.DataPointCollection;
import edu.cmu.sv.modelinference.common.formats.ValueTrackerProducer;
import edu.cmu.sv.modelinference.common.formats.st.STConfig;
import edu.cmu.sv.modelinference.common.formats.st.STEntry;
import edu.cmu.sv.modelinference.common.formats.st.STParser;
import edu.cmu.sv.modelinference.common.formats.st.STValueTracker;
import edu.cmu.sv.modelinference.common.formats.st.STValueTracker.FIELD;
import edu.cmu.sv.modelinference.common.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.common.parser.LogReader;
import edu.cmu.sv.modelinference.common.parser.SequentialLogReader;

/**
 * @author Kasper Luckow
 *
 */
public class STEventChartHandler implements LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>> {
  
  private static final Logger logger = LoggerFactory.getLogger(STEventChartHandler.class);
  private static final String FIELD_OPTS_ARG = "field";
  private static final String FLIGHTNAME_OPTS_ARG = "flightname";
  
  private boolean hasFlightName = false;
  private FIELD trackedField = null;
  private String flightName;
  private final Options cmdOpts;
  
  
  private static STEventChartHandler instance = null;
  
  public static STEventChartHandler getInstance() {
    if(instance == null) {
      instance = new STEventChartHandler();
    }
    return instance;
  }
  
  private STEventChartHandler() {
    this.cmdOpts = createCmdOptions();
  }
  
  private Options createCmdOptions() {
    Options options = new Options();
    
    Option addOpts = Option.builder(FIELD_OPTS_ARG).argName("Field").hasArg()
    		//Could make a list here based on values of STValueTracker.FIELD
          .desc("Field to be tracked, e.g., speed").required(true).build();
    
    Option flightNameOpt = Option.builder(FLIGHTNAME_OPTS_ARG).argName("Flight name").hasArg()
          .desc("Filter out everything but this flight name").required(false).build();

    options.addOption(addOpts);
    options.addOption(flightNameOpt);
    return options;
  }
  
  @Override
  public String getHandlerName() {
    return STConfig.LOG_CONFIG_NAME;
  }

  @Override
  public ValueTrackerProducer<?, DataPointCollection, ?> process(String logFile, String logType, String[] additionalCmdArgs) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch(ParseException exp) {
      logger.error(exp.getMessage());
      System.err.println(exp.getMessage());
      Util.printHelpAndExit(STEventChartHandler.class, cmdOpts);
    }

    try {
      trackedField = FIELD.valueOf(cmd.getOptionValue(FIELD_OPTS_ARG).toUpperCase());
    } catch(Exception exp) {
      logger.error(exp.getMessage());
      System.err.println(exp.getMessage());
      Util.printHelpAndExit(STEventChartHandler.class, cmdOpts);
    }
    
    if(cmd.hasOption(FLIGHTNAME_OPTS_ARG)) {
      hasFlightName = true;
      flightName = cmd.getOptionValue(FLIGHTNAME_OPTS_ARG); //e.g. USA5596
    }
    
    LogReader<STEntry> reader = null;
    if(hasFlightName) {
      LogEntryFilter<STEntry> filter =
          new LogEntryFilter<STEntry>() {
            @Override
            public boolean submitForProcessing(STEntry entry) {
              if(entry.getCallSign().equals(flightName)) {
                return true;
              }    
              return false;
            }
          };
      reader = new SequentialLogReader<>(new STParser(), filter);
    } else
      reader = new SequentialLogReader<>(new STParser());
    
    return new STValueTracker.STDataPointsGenerator(trackedField, reader);
  }
}
