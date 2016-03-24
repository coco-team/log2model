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
package edu.cmu.sv.modelinference.features.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeMap;

import com.google.common.collect.SortedMultiset;

/**
 * @author Kasper Luckow
 *
 */
public class EventClass implements Iterable<Event> {
  private final SortedMultiset<Event> events;
  private final String id;
  
  private double avgFeat;
  private boolean isAvgComputed = false;
  private double varianceFeat;
  private boolean isVarianceComputed = false;
  
  private static int AUTO_ID;
  
  public EventClass(SortedMultiset<Event> events) {
    this(""+getUniqueId(), events);
  }
  public EventClass(String classId, SortedMultiset<Event> events) {
    this.id = classId;
    this.events = events;
  }
  
  public String getClassId() {
    return this.id;
  }

  public double getAvgFeatureVal() {
    if(!isAvgComputed) {
      double sum = 0.0;
      for(Event evt : events)
        sum += evt.getFeature().getData();
      this.avgFeat = sum / (double) events.size();
      this.isAvgComputed = true;
    }
    return this.avgFeat;
  }
  
  public double getMinFeatureVal() {
    return this.events.firstEntry().getElement().getFeature().getData();
  }
  
  public double getMaxFeatureVal() {
    return this.events.lastEntry().getElement().getFeature().getData();
  }
  
  public double getFeatureVariance() {
    if(!isVarianceComputed) {
      double avg = getAvgFeatureVal();
      double sqDev = 0;
      for(Event evt : events)
        sqDev += Math.pow(evt.getFeature().getData() - avg, 2);
      //It does not seem clear whether we should divide by the data size
      //or data size - 1
      this.varianceFeat = sqDev / (double)(events.size());
      this.isVarianceComputed = true;
    }
    return this.varianceFeat;
  } 
  
  public Collection<Event> getEvents() {
    return this.events;
  }
  
  private static int getUniqueId() {
    return AUTO_ID++;
  }
  
  @Override
  public Iterator<Event> iterator() {
    return this.events.iterator();
  }
}
