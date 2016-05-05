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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.Log2EventChart;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;

/**
 * @author Kasper Luckow
 * Make this a log handler
 */
public class Main {

  public static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  private static Set<LogHandler<Void>> logHandlers = new HashSet<>();
  public static void registerLogHandler(LogHandler<Void> handler) {
    logHandlers.add(handler);
  }
  
  private static final String LOG_FILE_ARG = "input";
  private static final String INPUT_TYPE_ARG = "type";
  private static final String TOOL_TYPE_ARG = "tool";
  
  public static void main(String[] args) throws ParseException {
    logHandlers.add(new Log2EventChart());
    
    Options cmdOpts = createCmdOptions();
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(cmdOpts, args, true);
    
    String inputType = cmd.getOptionValue(INPUT_TYPE_ARG);
    String tool = cmd.getOptionValue(TOOL_TYPE_ARG);
    String logFile = cmd.getOptionValue(LOG_FILE_ARG);
    
    
    LogHandler<Void> logHandler = null;
    boolean found = false;
    for(LogHandler<Void> lh : logHandlers) {
      if(lh.getHandlerName().equals(tool)) {
        logHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<Void>> logIter = logHandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find tool for arg " + tool);
      throw new LogProcessingException("Supported tools: " + getSupportedToolsString());
    }
    logger.info("Using loghandler for logtype: " + logHandler.getHandlerName());
    
    
    logHandler.process(logFile, inputType, cmd.getArgs());
  }
  
  private static Options createCmdOptions() {
    Options options = new Options();
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
        .argName(getSupportedToolsString())
        .hasArg()
        .required()
        .desc("Specify which tool to use")
        .build();
    

    options.addOption(toolOpts);
    options.addOption(input);
    options.addOption(inputType);
    return options;
  }
  
  private static String getSupportedToolsString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    Iterator<LogHandler<Void>> logIter = logHandlers.iterator();
    while(logIter.hasNext()) {
      sb.append(logIter.next().getHandlerName());
      if(logIter.hasNext())
        sb.append(" | ");
    }
    sb.append(">");   
    return sb.toString();
  }
}
