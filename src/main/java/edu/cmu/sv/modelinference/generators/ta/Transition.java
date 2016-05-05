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
package edu.cmu.sv.modelinference.generators.ta;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.TreeMultiset;
/**
 * @author Kasper Luckow
 */
public class Transition {
  
  
  public static void main(String[] args) {
    Transition t = new Transition();
    
    t.addStep(3);
    System.out.println(t.toString());
    t.addStep(4);
    System.out.println(t.toString());
    t.addStep(6);
    System.out.println(t.toString());
    t.addStep(5);
    System.out.println(t.toString());
    t.addStep(2);
    System.out.println(t.toString());
    
    
    t.addStep(9);
    System.out.println(t.toString());
    t.addStep(10);
    System.out.println(t.toString());
    t.addStep(7);
    System.out.println(t.toString());
    t.addStep(12);
    System.out.println(t.toString());
    t.addStep(11);
    System.out.println(t.toString());
    t.addStep(8);
    System.out.println(t.toString());
  }

  private NavigableMap<Integer, Range> constraints = new TreeMap<>();
  
  public void addStep(int timeStep) {
    Map.Entry<Integer, Range> entry = constraints.floorEntry(timeStep);
    
    if(entry == null) {
      // Too small or initial insert
      Map.Entry<Integer, Range> nextEntry = constraints.floorEntry(timeStep + 1);
      if(nextEntry != null) { //expansion
        Range constraint = constraints.remove(nextEntry.getKey());
        constraints.put(timeStep, constraint);
        constraint.getValue().add(timeStep);
      } else
        createNewConstraint(timeStep);
    } else {
      if(timeStep <= entry.getValue().getUpperBound() + 1) {
        entry.getValue().getValue().add(timeStep);
      } else {
        // Too large or in a hole
        createNewConstraint(timeStep);
      }
      
      // compaction
      Map.Entry<Integer, Range> nextEntry = constraints.floorEntry(entry.getValue().getValue().lastEntry().getElement() + 1);
      if(!nextEntry.equals(entry)) {
        TreeMultiset<Integer> high = constraints.remove(nextEntry.getKey()).getValue();
        entry.getValue().getValue().addAll(high);
      }
    }
  }
  
  private void createNewConstraint(int timeStep) {
    TreeMultiset<Integer> constraint = TreeMultiset.create();
    constraint.add(timeStep);
    Range range = new Range(constraint);
    constraints.put(timeStep, range);
  }
  
  @Override
  public String toString() {
    String s  = "";
    for (Map.Entry<Integer, Range> entry : constraints.entrySet()) {
      s += + entry.getKey() + " --> (" + entry.getValue().toString() + "), ";
    }
    return s.substring(0,s.length()-2);
  }
}
