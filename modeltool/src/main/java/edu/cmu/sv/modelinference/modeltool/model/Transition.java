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
public class Transition implements ModelElement {

  private final State src;
  private final State dest;
  
  public Transition(State src, State dest) {
    this.src = src;
    this.dest = dest;
    src.addOutgoingTransition(this);
    dest.addIncomingTransition(this);
  }
  
  public State getSource() {
    return this.src;
  }
  
  public State getDest() {
    return this.dest;
  }
  
  @Override
  public void accept(ModelVisitor visitor) {
    visitor.visit(this);
    this.dest.accept(visitor);
  }
}
