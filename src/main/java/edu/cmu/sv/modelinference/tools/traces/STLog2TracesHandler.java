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
package edu.cmu.sv.modelinference.tools.traces;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.Main;
import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ModelInferer;
import edu.cmu.sv.modelinference.generators.formats.st.Coord2d;
import edu.cmu.sv.modelinference.generators.formats.st.GridState;
import edu.cmu.sv.modelinference.generators.formats.st.STConfig;
import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.formats.st.STGridStateFactory;
import edu.cmu.sv.modelinference.generators.formats.st.STModelInferer;
import edu.cmu.sv.modelinference.generators.formats.st.STParser;
import edu.cmu.sv.modelinference.generators.formats.st.util.GridDimensionsFinder;
import edu.cmu.sv.modelinference.generators.formats.st.util.GridDimensionsFinder.Dimensions;
import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.generators.parser.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.trace.TimedTrace;
import edu.cmu.sv.modelinference.generators.trace.TraceGenerator;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
import edu.cmu.sv.modelinference.tools.cmdutil.Util.GridPartitions;
import edu.cmu.sv.modelinference.tools.model.Log2Model;

/**
 * @author Kasper Luckow
 *
 */
public class STLog2TracesHandler implements LogHandler<Collection<TimedTrace<GridState>>> {
  public static final Logger logger = LoggerFactory.getLogger(STLog2TracesHandler.class);
  
  private static final String GRID_DIM = "dim";
  
  private static STLog2TracesHandler instance = null;
  
  public static STLog2TracesHandler getInstance() {
    if(instance == null) {
      instance = new STLog2TracesHandler();
    }
    return instance;
  }
  
  private Options cmdOpts;
  
  private STLog2TracesHandler() {
    this.cmdOpts = createCmdOptions();
  }
  
  private Options createCmdOptions() {
    Options options = new Options();
    
    Option addOpts = Option.builder(GRID_DIM).argName("Grid Dimensions").hasArg()
          .desc("Dimensions of the grid projected on the airfield. Format: NUMxNUM.").build();
    
    options.addOption(addOpts);
    return options;
  }

  @Override
  public String getHandlerName() {
    return STConfig.LOG_CONFIG_NAME;
  }
  
  @Override
  public Collection<TimedTrace<GridState>> process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch (ParseException e) {
      logger.error(e.getMessage());
      System.err.println(e.getMessage());
      Util.printHelpAndExit(Log2Model.class, cmdOpts);
    }

    GridPartitions parts = null;
    if(cmd.hasOption(GRID_DIM)) {
      String partStr = cmd.getOptionValue(GRID_DIM).trim();
      try {
        parts = Util.extractGridPartitions(partStr);
      } catch (ParseException e) {
        throw new LogProcessingException(e);
      }
    } else
      parts = GridPartitions.createDefault();
    
    LogEntryFilter<STEntry> filter = LogEntryFilter.<STEntry>EVERYTHING();
    
    LogReader<STEntry> reader = new SequentialLogReader<>(new STParser(), filter);
    GridDimensionsFinder d;
    try {
      d = new GridDimensionsFinder();
    } catch (IOException e) {
      throw new LogProcessingException(e);
    }
    Dimensions dim;
    try {
      dim = d.start(new File(logFile));
    } catch (IOException e) {
      throw new LogProcessingException(e);
    }
    STGridStateFactory stateGen = new STGridStateFactory(
        new Coord2d(dim.minX, dim.minY), 
        new Coord2d(dim.maxX, dim.maxY), 
        parts.horiz, 
        parts.vert);
    
    TraceGenerator<STEntry, GridState> traceGenerator = new TraceGenerator<>(reader, stateGen);
    Collection<TimedTrace<GridState>> traces;
    try {
      traces = traceGenerator.computeTraces(new File(logFile));
    } catch (IOException e) {
      throw new LogProcessingException(e);
    }
    
    return traces;
  }
}
