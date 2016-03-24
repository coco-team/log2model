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

import edu.cmu.sv.modelinference.generators.model.State;

public class TimedState<S extends State> {
  private final S state;
  private double from = -1, to = -1;

  public TimedState(S state, double time) {
    this.state = state;
    this.from = this.to = time;
  }
  
  public S getState() {
    return this.state;
  }
  
  public void addTimeStamp(double stamp) {
    if(from < 0)
      from = stamp;
    to = stamp;
  }
  
  public double getFromTimeStamp() {
    return from;
  }

  public double getToTimeStamp() {
    return to;
  }
  
  public double getDuration() {
    return to - from;
  }
  
  @Override
  public String toString() {
    return this.state.toString() + "[" + from + "," + to +"]";
  }
}