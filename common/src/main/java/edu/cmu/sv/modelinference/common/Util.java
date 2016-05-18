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
package edu.cmu.sv.modelinference.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.cmu.sv.modelinference.common.api.LogHandler;

/**
 * @author Kasper Luckow
 *
 */
public class Util {
  public static String getSupportedHandlersString(Set<? extends LogHandler<?>> supportedHandlers) {
    StringBuilder sb = new StringBuilder();
    Iterator<? extends LogHandler<?>> logIter = supportedHandlers.iterator();
    while(logIter.hasNext()) {
      sb.append(logIter.next().getHandlerName());
      if(logIter.hasNext())
        sb.append(" | ");
    }
    return sb.toString();
  }
  
  public static void printHelpAndExit(Class<?> clz, Options opts) {
    printHelpAndExit(clz, opts, -1);
  }
  
  public static void printHelpAndExit(Class<?> clz, Options opts, int exitVal) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(clz.getName(), opts);
    System.exit(exitVal);
  }
}
