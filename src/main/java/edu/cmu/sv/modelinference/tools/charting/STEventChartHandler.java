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
package edu.cmu.sv.modelinference.tools.charting;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.generators.formats.st.STConfig;
import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.formats.st.STParser;
import edu.cmu.sv.modelinference.generators.formats.st.STValueTracker;
import edu.cmu.sv.modelinference.generators.formats.st.STValueTracker.FIELD;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.generators.parser.SequentialLogReader;
import edu.cmu.sv.modelinference.tools.LogHandler;

/**
 * @author Kasper Luckow
 *
 */
public class STEventChartHandler implements LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>> {
  
  private static final Logger logger = LoggerFactory.getLogger(STEventChartHandler.class);
  private static final String ADD_OPTS_ARG = "a";
  private static final String FLIGHTNAME_OPTS_ARG = "f";
  
  private boolean hasFlightName = false;
  private FIELD trackedField = null;
  private String flightName;
  private final Options cmdOpts;
  
  
  private static STEventChartHandler instance = new STEventChartHandler();
  public static STEventChartHandler getInstance() {
    return instance;
  }
  
  private STEventChartHandler() {
    this.cmdOpts = createCmdOptions();
  }
  
  private Options createCmdOptions() {
    Options options = new Options();
    
    Option addOpts = Option.builder(ADD_OPTS_ARG).argName("Additional options").hasArg()
          .desc("Additional input type options").build();
    
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
    CommandLine cmd;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch (ParseException e) {
      throw new LogProcessingException(e);
    }

    try {
      trackedField = FIELD.valueOf(cmd.getOptionValue(ADD_OPTS_ARG).toUpperCase());
    } catch(Exception e) {
      String msg = "Must be supplied a field to be tracked (e.g., pos_x) to additional arg option";
      logger.error(msg);
      throw new LogProcessingException(msg);
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
