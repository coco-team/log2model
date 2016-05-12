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
package edu.cmu.sv.modelinference.generators.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Kasper Luckow
 */
public class State implements ModelElement {

  protected List<Transition> in = new ArrayList<>();
  protected List<Transition> out = new ArrayList<>();
  
  protected List<Assignment<? extends Number>> assignments = new ArrayList<>();

  private boolean isVisited = false;

  //Instead of relying on a "stateful" visitor
  //where we need to reset the visited flag, iterate
  //over the states instead because this is ugly
  public void setVisited(boolean flag) {
    this.isVisited = flag;
  }
  
  @Override
  public void accept(ModelVisitor visitor) {
    if(!isVisited) {
      visitor.visit(this);
      isVisited = true;
      for(Transition o : this.out) {
        o.accept(visitor);
      }
    }
  }
  
  public List<Assignment<? extends Number>> getAssignments() {
    return this.assignments;
  }
  
  public Assignment<?> getAssignment(String name) {
    for(Assignment<?> var : this.assignments) {
      if(var.getName().equals(name))
        return var;
    }
    return null;
  }
  
  public <T extends Number> Collection<T> getAssignmentsOfType(Class<T> clazz) {
    Collection<T> vars = new LinkedList<>();
    for(Assignment<?> var : this.assignments) {
      if(clazz.isInstance(var)) {
        vars.add(clazz.cast(var));
      }
    }
    return vars;
  }
  
  public List<Transition> getOutgoingTransitions() {
    return this.out;
  }
  
  public List<Transition> getIncomingTransitions() {
    return this.in;
  }
  
  public boolean containsSucessor(State state) {
    return this.getSuccessorStates().contains(state);
  }
  
  public boolean containsPredecessor(State state) {
    return this.getPredecessorStates().contains(state);
  }
  
  public List<State> getPredecessorStates() {
    List<State> pred = new ArrayList<>();
    for(Transition in : this.in)
      pred.add(in.getSource());
    return pred;
  }
  
  public Transition getTransitionForDestination(State dest) {
    for(Transition out : this.getOutgoingTransitions()) {
      if(out.getDest().equals(dest))
        return out;
    }
    return null;
  }
  
  public List<State> getSuccessorStates() {
    List<State> succ = new ArrayList<>();
    for(Transition out : this.out)
      succ.add(out.getDest());
    return succ;
  }
  
  public void addAssignment(Assignment<? extends Number> assign) {
    this.assignments.add(assign);
  }
  
  public void addAllAssignments(Collection<Assignment<? extends Number>> assigns) {
    this.assignments.addAll(assigns);
  }
  
  public void addIncomingTransition(Transition in) {
    this.in.add(in);
  }
  
  public void addOutgoingTransition(Transition out) {
    this.out.add(out);
  }

  public int getStateId() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean equals(Object other) {
    if(other instanceof State) {
      final State otherState = (State) other;
      Set<Object> set1 = new HashSet<Object>();
      set1.addAll(this.assignments);
      Set<Object> set2 = new HashSet<Object>();
      set2.addAll(otherState.assignments);
      return set1.equals(set2);
    } else
      return false;
  }
  
  @Override
  public int hashCode(){
      return Objects.hashCode(this.assignments);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Iterator<Assignment<? extends Number>> varit = this.assignments.iterator();
    while(varit.hasNext()) {
      sb.append(varit.next());
      if(varit.hasNext())
        sb.append("\n");
    }
    return sb.toString();
  }

}
