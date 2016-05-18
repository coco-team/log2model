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
package edu.cmu.sv.modelinference.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.TreeMultiset;


/**
 * @author Kasper Luckow
 */
public class Model<T extends State> implements ModelElement {

  private Set<T> states = new HashSet<>();
  private T initState;
  private Set<String> modelVariables = new HashSet<>();

  public void addState(T state) {
    if(states.contains(state))
      return;
    if(initState == null)
      initState = state;
    for(Assignment<?> v : state.getAssignments())
      modelVariables.add(v.getName());
    this.states.add(state);
  }

  public Set<String> getModelVariables() {
    return this.modelVariables;
  }

  public Set<T> getStates() {
    return this.states;
  }

  public boolean containsState(T state) {
    return this.states.contains(state);
  }

  //TODO: This is a bad one performance-wise....
  public T getState(T state) {
    for(T s : this.states) {
      if(s.equals(state))
        return s;
    }
    return null;
  }

  public T getInitState() {
    return this.initState;
  }

  //TODO: super ugly that value of variables need to be treated as strings because they are objects!! Fix
  //TODO: super ugly dealing with variables as Variable and Strings.. fix
  public int getMaxAssignedVal(String modelVar) {
    int max = Integer.MIN_VALUE;
    //TODO: super expensive
    for(T s : this.getStates()) {
      Assignment<?> var = s.getAssignment(modelVar);
      if(var != null) {
        int val = Integer.valueOf(var.getValue().toString());
        if(val > max)
          max = val;
      }
    }
    return max;
  }

  //TODO: super ugly that value of variables need to be treated as strings because they are objects!! Fix
  //TODO: super ugly dealing with variables as Variable and Strings.. fix
  public int getMinAssignedVal(String modelVar) {
    int min = Integer.MAX_VALUE;
    //TODO: super expensive
    for(T s : this.getStates()) {
      Assignment<?> var = s.getAssignment(modelVar);
      if(var != null) {
        int val = Integer.valueOf(var.getValue().toString());
        if(val < min)
          min = val;
      }

    }
    return min;
  }

  @Override
  public void accept(ModelVisitor visitor) {
    visitor.visit(this);
    this.initState.accept(visitor);
  }

  public int getStateSpaceSize() {
    return states.size();
  }
}
