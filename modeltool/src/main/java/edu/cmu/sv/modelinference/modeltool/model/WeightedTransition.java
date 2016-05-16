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
package edu.cmu.sv.modelinference.modeltool.model;

/**
 * @author Kasper Luckow
 */
public class WeightedTransition extends Transition {

  private int weight = 1;

  public WeightedTransition(State src, State dest) {
    super(src, dest);
  }
  
  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  public int getWeight() {
    return this.weight;
  }
  
  public double getNormalizedWeight() {
    State source = this.getSource();
    double totalWeight = 0.0;
    for(Transition t : source.getOutgoingTransitions())
      totalWeight += ((WeightedTransition)t).getWeight();
    return this.weight / totalWeight;
  }
  
  public void incrementWeight(int inc) {
    this.weight += inc;
  }
}
