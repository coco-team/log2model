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

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.generators.formats.autoresolver.ARValueTracker;
import edu.cmu.sv.modelinference.generators.formats.autoresolver.AutoresolverConfig;
import edu.cmu.sv.modelinference.generators.formats.autoresolver.AutoresolverEntry;
import edu.cmu.sv.modelinference.generators.formats.autoresolver.AutoresolverParser;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.generators.parser.SequentialLogReader;
import edu.cmu.sv.modelinference.tools.LogHandler;

/**
 * @author Kasper Luckow
 *
 */
public class AREventChartHandler implements LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>>{
  
  private static final Logger logger = LoggerFactory.getLogger(AREventChartHandler.class);
  private static final String ADD_OPTS_ARG = "a";
  
  private final Options cmdOpts;
  
  private static final AREventChartHandler instance = new AREventChartHandler();
  
  public static AREventChartHandler getInstance() {
    return instance;
  }

  private AREventChartHandler() {
    this.cmdOpts = createCmdOptions();
  }
  
  private Options createCmdOptions() {
    Options options = new Options();
    
    Option addOpts = Option.builder(ADD_OPTS_ARG).argName("Additional options").hasArg()
          .desc("Additional input type options").build();
   
    options.addOption(addOpts);
    return options;
  }
  
  @Override
  public String getHandlerName() {
    return AutoresolverConfig.LOG_CONFIG_NAME;
  }

  @Override
  public ValueTrackerProducer<?, DataPointCollection, ?> process(String logFile, String logType,
      String[] additionalCmdArgs) throws LogProcessingException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch (ParseException e) {
      throw new LogProcessingException(e);
    }
    ARValueTracker.FIELD trackedFieldAR =null;
    try {
      trackedFieldAR = ARValueTracker.FIELD.valueOf(cmd.getOptionValue(ADD_OPTS_ARG).toUpperCase());
    } catch(Exception e) {
      logger.error(e.getMessage());
      logger.error("Must be supplied a field to be tracked (e.g., pos_x) to additional arg option");
      System.exit(-1);
    }
    
    LogReader<AutoresolverEntry> readerAR = new SequentialLogReader<>(new AutoresolverParser());
    return new ARValueTracker.ARDataPointsGenerator(trackedFieldAR, readerAR);
  }
}
