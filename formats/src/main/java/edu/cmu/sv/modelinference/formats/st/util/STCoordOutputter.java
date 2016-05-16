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
package edu.cmu.sv.modelinference.generators.formats.st.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.LogProcessor;
import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.formats.st.STParser;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.generators.parser.SequentialLogReader;

/**
 * @author Kasper Luckow
 */
public class STCoordOutputter implements LogProcessor<STEntry> {
  private PrintWriter writer;
  private LogReader<STEntry> stReader;
  
  public STCoordOutputter(LogEntryFilter<STEntry> filter) throws IOException {
    stReader = new SequentialLogReader<>(new STParser(), filter);
    stReader.addLogProcessor(this);
    
  }
  
  public void start(File logFile, File outputFile) throws IOException {
    this.writer = new PrintWriter(outputFile, "UTF-8");
    stReader.parseLog(logFile);
    this.writer.close();
  }
  
  @Override
  public void process(STEntry entry) {
    this.writer.println(entry.getPosition().getX() + ", " + entry.getPosition().getY());
  }
}
