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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.LogProcessor;
import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.formats.st.STParser;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.generators.parser.SequentialLogReader;

/**
 * @author Kasper Luckow
 */
public class DelayFinder implements LogProcessor<STEntry> {
  
  public static class Delay {
    public double start;
    public double end;
    
    public Delay(double start) {
      this.start = start;
      this.end = start;
    }
    
    public double getDelay() {
      return this.end - this.start;
    }
  }
  
  private Map<String, Delay> vehicleDelay;
  private final LogReader<STEntry> stReader;
  
  public DelayFinder(LogEntryFilter<STEntry> filter) throws IOException {
    stReader = new SequentialLogReader<>(new STParser(), filter);
    stReader.addLogProcessor(this);
  }
  
  public void start(String logFile) throws IOException {
    vehicleDelay = new HashMap<>();
    stReader.parseLog(logFile);
  }
  
  @Override
  public void process(STEntry entry) {
    String vehicleName = entry.getCallSign();
    double timestamp = entry.getLogTime();
    if(!this.vehicleDelay.containsKey(vehicleName)) {
      this.vehicleDelay.put(vehicleName, new Delay(timestamp));
    } else {
      Delay d = this.vehicleDelay.get(vehicleName);
      if(timestamp < d.start)
        d.start = timestamp;
      if(timestamp > d.end)
        d.end = timestamp;
    }
  } 
  
  public Map<String, Delay> getDelays() {
    return this.vehicleDelay;
  }
}
