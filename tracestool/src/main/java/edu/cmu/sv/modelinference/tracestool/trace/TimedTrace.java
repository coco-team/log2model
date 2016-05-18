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
package edu.cmu.sv.modelinference.tracestool.trace;

import java.util.Iterator;
import java.util.LinkedList;

import edu.cmu.sv.modelinference.common.model.State;

/**
 * @author Kasper Luckow
 */
public class TimedTrace<S extends State> implements Iterable<TimedState<S>>{
  private LinkedList<TimedState<S>> trace = new LinkedList<>();
  private final String id;
  
  public TimedTrace(String id) {
    this.id = id;
  }
  
  public TimedState<S> getLast() {
    return this.trace.getLast();
  }
  
  public void addState(TimedState<S> state) {
    this.trace.add(state);
  }

  @Override
  public Iterator<TimedState<S>> iterator() {
    return this.trace.iterator();
  }
  
  @Override
  public String toString() {
    Iterator<TimedState<S>> iter = iterator();
    StringBuilder sb = new StringBuilder();
    sb.append(id).append(": ");
    while(iter.hasNext()) {
      sb.append(iter.next().toString());
      if(iter.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }
}