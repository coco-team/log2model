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
package edu.cmu.sv.modelinference.generators.parser.st.util;

import java.io.IOException;
import java.util.HashMap;

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.LogProcessor;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;
import edu.cmu.sv.modelinference.generators.parser.reader.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.parser.st.STEntry;
import edu.cmu.sv.modelinference.generators.parser.st.STParser;

/**
 * @author Kasper Luckow
 */
public class GridDimensionsFinder implements LogProcessor<STEntry> {
  
  public static class Dimensions {
    public double maxX = Double.MIN_VALUE;
    public double maxY = Double.MIN_VALUE;
    public double minX = Double.MAX_VALUE;
    public double minY = Double.MAX_VALUE;
  }
  
  private final LogReader<STEntry> stReader;
  private Dimensions dim;
  
  public GridDimensionsFinder() throws IOException {
    stReader = new SequentialLogReader<>(new STParser());
    stReader.addLogProcessor(this);
  }
  
  public GridDimensionsFinder(LogEntryFilter<STEntry> filter) throws IOException {
    stReader = new SequentialLogReader<>(new STParser(), filter);
    stReader.addLogProcessor(this);
  }
  
  public Dimensions start(String logFile) throws IOException {
    this.dim = new Dimensions();
    stReader.parseLog(logFile);
    return this.dim;
  }
  
  @Override
  public void process(STEntry entry) {
    double x = entry.getPosition().getX(); 
    if(x > dim.maxX)
      dim.maxX = x;
    if(x < dim.minX)
      dim.minX = x;
    
    double y = entry.getPosition().getY(); 
    if(y > dim.maxY)
      dim.maxY = y;
    if(y < dim.minY)
      dim.minY = y;
  }
}
