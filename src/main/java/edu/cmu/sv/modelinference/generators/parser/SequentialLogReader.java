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
package edu.cmu.sv.modelinference.generators.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.cmu.sv.modelinference.generators.LogEntry;
import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.LogParser;
import edu.cmu.sv.modelinference.generators.ParserException;

/**
 * @author Kasper Luckow
 */
public class SequentialLogReader<T extends LogEntry> extends LogReader<T> {

  public SequentialLogReader(LogParser<T> logParser) {
    super(logParser);
  }
  
  public SequentialLogReader(LogParser<T> logParser, LogEntryFilter<T> filter) {
    super(logParser, filter);
  }

  @Override
  public void parseLog(File logFile) throws IOException {
    FileReader fr = new FileReader(logFile);
    try(BufferedReader rd = new BufferedReader(fr)) {
      String logEntry;
      while((logEntry = rd.readLine()) != null) {
        try {
          super.processRawEntry(logEntry);
        } catch (ParserException e) {
          //this is really bad... Maybe change the convention of the parser, e.g., return null
        }
      }
    }
  }
}
