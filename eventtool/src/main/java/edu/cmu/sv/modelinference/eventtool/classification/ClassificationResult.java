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
package edu.cmu.sv.modelinference.eventtool.classification;

import java.util.AbstractMap.SimpleEntry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.collect.Range;

/**
 * @author Kasper Luckow
 *
 */
public class ClassificationResult {
  private final Collection<EventClass> results;
  
  public ClassificationResult(Collection<EventClass> results) {
    this.results = results;
  }
  
  public Collection<EventClass> getEventClasses() {
    return results;
  }
  
  /**
   * returns a sorted set.
   * Yes, I wish treemap would return SortedSet instead of Set, 
   * because Set does not provide guarantee on iteration order even 
   * though the Set returned by entrySet does indeed preserve natural order...
   */
  public Set<Map.Entry<Event, EventClass>> getEvtSeqWithClassifiers() {
    SortedMap<Event, EventClass> evtMap = new TreeMap<>(new Comparator<Event>() {
      @Override
      public int compare(Event o1, Event o2) {
        Range<Integer> o1Range = o1.getRange();
        Range<Integer> o2Range = o2.getRange();
        return o1Range.lowerEndpoint() - o2Range.lowerEndpoint();
      }
    });
    
    for(EventClass cl : results) {
      for(Event evt : cl) {
        evtMap.put(evt, cl);
      }
    }
    return evtMap.entrySet();
  }
  
  public XYSeriesCollection getSeries() {
    XYSeriesCollection clusterSeriesCollection = new XYSeriesCollection();
    for(EventClass evtCl : results) {
      XYSeries pl = new XYSeries(evtCl.getClassId());
      for(Event data : evtCl.getEvents()) {
        pl.add(0, data.getFeature().getData());
      }
      clusterSeriesCollection.addSeries(pl);
    }
    return clusterSeriesCollection;
  }
}
