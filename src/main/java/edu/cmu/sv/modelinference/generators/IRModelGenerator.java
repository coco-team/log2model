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
package edu.cmu.sv.modelinference.generators;

import java.io.File;
import java.io.IOException;

import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.generators.model.State;
import edu.cmu.sv.modelinference.generators.model.Transition;
import edu.cmu.sv.modelinference.generators.model.WeightedTransition;
import edu.cmu.sv.modelinference.generators.parser.LogReader;

/**
 * @author Kasper Luckow
 */
public final class IRModelGenerator<T extends LogEntry, S extends State> implements LogProcessor<T> {

  protected Model<S> model;
  protected S prevState = null, currState = null;
  private double currentTime = -1.0;
  
  private StateFactory<T, S> stateGenerator;
  private LogReader<T> logReader;
  
  public IRModelGenerator(LogReader<T> logReader, StateFactory<T, S> stateGenerator) {
    this.stateGenerator = stateGenerator;
    this.logReader = logReader;
    this.logReader.addLogProcessor(this);
  }
  
  public Model<S> generateModel(String logFile) throws IOException {
    model = new Model<>();
    prevState = null; 
    currState = null;
    currentTime = -1.0;
    
    this.logReader.parseLog(new File(logFile));
    
    return model;
  }
  
  @Override
  public void process(T entry) {
    if(isNewState(entry)) {
      if(currState != null) {
        //this seems a little bit odd but is due to how equals is implemented now
        S s = this.model.getState(currState); 
        if(s != null)
          currState = s;
        else
          this.model.addState(currState);
      }
      if(prevState != null && currState != null)
        generateTransition(prevState, currState);
      prevState = currState;
      currState = stateGenerator.generateState(currState, entry);
    }
    if(entry != null)
      stateGenerator.addEntryToState(currState, entry);    
  }

  private boolean isNewState(T entry) {
    if(entry.getLogTime() > this.currentTime) {
      this.currentTime = entry.getLogTime();
      return true;
    }
    return false;
  }
  
  private void generateTransition(State prevState, State currState) {
    Transition ct = prevState.getTransitionForDestination(currState);
    if(ct != null) {
      if(ct instanceof WeightedTransition) {
        ((WeightedTransition)ct).incrementWeight(1);
      } else
        throw new IllegalStateException("Expected transition of type: " + WeightedTransition.class.getName());
    } else
      new WeightedTransition(prevState, currState);
  }
}
