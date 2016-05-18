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
package edu.cmu.sv.modelinference.common.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.sv.modelinference.common.generators.LogEntry;
import edu.cmu.sv.modelinference.common.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.common.generators.LogParser;
import edu.cmu.sv.modelinference.common.generators.LogProcessor;
import edu.cmu.sv.modelinference.common.generators.ParserException;

/**
 * @author Kasper Luckow
 */
public abstract class LogReader<T extends LogEntry> {
  protected LogEntryFilter<T> filter = null;
  protected Set<LogProcessor<T>> logProcessors = new HashSet<>();
  protected LogParser<T> logParser;
  
  public LogReader(LogParser<T> logParser) {
    this.logParser = logParser;
  }
  
  public LogReader(LogParser<T> logParser, LogEntryFilter<T> filter) {
    this.filter = filter;
    this.logParser = logParser;
  }
  
  public void addLogProcessor(LogProcessor<T> logProcessor) {
    this.logProcessors.add(logProcessor);
  }
  
  public abstract void parseLog(File log) throws IOException;
  
  protected final void processRawEntry(String rawEntry) throws ParserException {
    T entry = logParser.parse(rawEntry);
    if(filter == null || filter.submitForProcessing(entry)) {
      for(LogProcessor<T> proc : logProcessors)
        proc.process(entry);
    }
  }
}
