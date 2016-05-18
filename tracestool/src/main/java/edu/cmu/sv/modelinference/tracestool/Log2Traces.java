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
package edu.cmu.sv.modelinference.tracestool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import edu.cmu.sv.modelinference.common.api.LogProcessingException;
import edu.cmu.sv.modelinference.common.formats.st.GridState;
import edu.cmu.sv.modelinference.tracestool.trace.TimedTrace;

/**
 * @author Kasper Luckow
 */
public class Log2Traces implements LogHandler<Void> {
  private static final String HELP_ARG = "help";
  private static final String OUTPUT_ARG = "o";
  
  private static final Logger logger = LoggerFactory.getLogger(Log2Traces.class.getName());
      
  private static Set<LogHandler<Collection<TimedTrace<GridState>>>> loghandlers = new HashSet<>();
  
  static {
    loghandlers.add(STLog2TracesHandler.getInstance());
  }
  
  private static Log2Traces instance = null;
  
  public static Log2Traces getInstance() {
    if(instance == null) {
      instance = new Log2Traces();
    }
    return instance;
  }
  
  private Options cmdOpts;
  
  private Log2Traces() {
    cmdOpts = createCmdOptions();
  }
  
  public static Options createCmdOptions() {
    Options options = new Options();
    
    Option help = new Option(HELP_ARG, "print this message");

    Option output = Option.builder(OUTPUT_ARG).argName("file")
                                .hasArg()
                                .desc("Specify output file.")
                                .required()
                                .build();

    options.addOption(help);
    options.addOption(output);
    return options;
  }

  @Override
  public String getHandlerName() {
    return "traces";
  }

  @Override
  public Void process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, true);
    } catch(ParseException exp) {
      logger.error(exp.getMessage());
      System.err.println(exp.getMessage());
      Util.printHelpAndExit(Log2Traces.class, cmdOpts);
    }
    
    if(cmd.hasOption(HELP_ARG))
      Util.printHelpAndExit(Log2Traces.class, cmdOpts, 0);
    

    LogHandler<Collection<TimedTrace<GridState>>> logHandler = null;
    boolean found = false;
    for(LogHandler<Collection<TimedTrace<GridState>>> lh : loghandlers) {
      if(lh.getHandlerName().equals(logType)) {
        logHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<Collection<TimedTrace<GridState>>>> logIter = loghandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find loghandler for " + logType);
      System.err.println("Supported log handlers: " + sb.toString());
      Util.printHelpAndExit(Log2Traces.class, cmdOpts);
    }

    String outputPath = cmd.getOptionValue(OUTPUT_ARG);
    
    Collection<TimedTrace<GridState>> traces = logHandler.process(logFile, logType, cmd.getArgs());

    try(PrintWriter pw = new PrintWriter(new File(outputPath))) {
      for(TimedTrace<GridState> trace : traces) {
        pw.println(trace.toString());
      }
    } catch (FileNotFoundException e) {
      throw new LogProcessingException(e);
    }  
    return null;
  }
}
