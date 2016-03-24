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
package edu.cmu.sv.modelinference.generators.trace;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sv.modelinference.generators.LogEntry;
import edu.cmu.sv.modelinference.generators.LogProcessor;
import edu.cmu.sv.modelinference.generators.StateFactory;
import edu.cmu.sv.modelinference.generators.model.State;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;

public class TraceGenerator<T extends LogEntry, S extends State> implements LogProcessor<T> {

  private StateFactory<T, S> stateGenerator;
  private LogReader<T> logReader;
  private Map<String, TimedTrace<S>> id2traces = new HashMap<>();
  
  public TraceGenerator(LogReader<T> logReader, StateFactory<T, S> stateGenerator) {
    this.stateGenerator = stateGenerator;
    this.logReader = logReader;
    this.logReader.addLogProcessor(this);
  }
  
  public Collection<TimedTrace<S>> computeTraces(String logFile) throws IOException {
    this.logReader.parseLog(logFile);
    return this.id2traces.values();    
  }
  
  @Override
  public void process(T entry) {
    double currTime = entry.getLogTime();
    String id = entry.getLogProducerId();
    TimedTrace<S> trace = id2traces.get(id);
    if(trace == null) { // first time this producer id has been encountered
      trace = new TimedTrace<>(id);
      id2traces.put(id, trace);
      TimedState<S> state = new TimedState<>(stateGenerator.generateState(null, entry), currTime);
      trace.addState(state);
      return;
    }
    
    TimedState<S> currState = trace.getLast();
    double prevTime = currState.getToTimeStamp();
    if(currTime > prevTime) {
      if(stateGenerator.isNewState(currState.getState(), entry)) {
        currState = new TimedState<>(stateGenerator.generateState(currState.getState(), entry), currTime);
        trace.addState(currState);
      } else
        currState.addTimeStamp(currTime);
    } else {
      stateGenerator.addEntryToState(currState.getState(), entry);
      currState.addTimeStamp(currTime);
    }
  }
}
