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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.Log2EventChart;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
import edu.cmu.sv.modelinference.tools.model.Log2Model;
import edu.cmu.sv.modelinference.tools.traces.Log2Traces;

/**
 * @author Kasper Luckow
 * Make this a log handler
 */
public class Main {

  public static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  private static Set<LogHandler<?>> logHandlers = new HashSet<>();
  public static void registerLogHandler(LogHandler<Void> handler) {
    logHandlers.add(handler);
  }
  
  //Put all log handlers here.
  static {
    logHandlers.add(Log2EventChart.getInstance());
    logHandlers.add(Log2Traces.getInstance());
    logHandlers.add(Log2Model.getInstance());
  }
  
  private static final String LOG_FILE_ARG = "input";
  private static final String INPUT_TYPE_ARG = "type";
  private static final String TOOL_TYPE_ARG = "tool";

  private static final String HELP_ARG = "help";
  
  public static void main(String[] args) {
    Options cmdOpts = createCmdOptions();
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(cmdOpts, args, true);
    } catch(ParseException exp) {
      logger.error(exp.getMessage());
      System.err.println(exp.getMessage());
      Util.printHelpAndExit(Main.class, cmdOpts);
    }
    
    String inputType = cmd.getOptionValue(INPUT_TYPE_ARG);
    String tool = cmd.getOptionValue(TOOL_TYPE_ARG);
    String logFile = cmd.getOptionValue(LOG_FILE_ARG).replaceFirst("^~",System.getProperty("user.home"));

    LogHandler<?> logHandler = null;
    boolean found = false;
    for(LogHandler<?> lh : logHandlers) {
      if(lh.getHandlerName().equals(tool)) {
        logHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<?>> logIter = logHandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find tool for arg " + tool);
      
      System.err.println("Supported tools: " + Util.getSupportedHandlersString(logHandlers));
      Util.printHelpAndExit(Main.class, cmdOpts);
    }
    logger.info("Using loghandler for logtype: " + logHandler.getHandlerName());

    logHandler.process(logFile, inputType, cmd.getArgs());
  }
  
  private static Options createCmdOptions() {
    Options options = new Options();
    Option help = new Option(HELP_ARG, "print this message");
    
    Option inputType = Option.builder(INPUT_TYPE_ARG)
        .argName("Log type (tool dependent)")
        .hasArg()
        .desc("Specify log type")
        .required()
        .build();
    
    Option input = Option.builder(LOG_FILE_ARG)
                          .argName("File")
                          .hasArg()
                          .desc("Specify input log file.")
                          .required()
                          .build();
    
    Option toolOpts = Option.builder(TOOL_TYPE_ARG)
        .argName(Util.getSupportedHandlersString(logHandlers))
        .hasArg()
        .required()
        .desc("Specify which tool to use")
        .build();

    options.addOption(help);
    options.addOption(toolOpts);
    options.addOption(input);
    options.addOption(inputType);
    return options;
  }
}
