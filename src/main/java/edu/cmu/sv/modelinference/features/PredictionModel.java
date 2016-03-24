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
package edu.cmu.sv.modelinference.features;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.google.common.collect.Range;

/**
 * @author Kasper Luckow
 *
 */

public class PredictionModel {
  private Map<Integer, Double> upperThreshold;
  private Map<Integer, Double> lowerThreshold;
  
  PredictionModel(Map<Integer, Double> upperThreshold, Map<Integer, Double> lowerThreshold) {
    checkArgument(upperThreshold.size() == lowerThreshold.size());
    this.upperThreshold = upperThreshold;
    this.lowerThreshold = lowerThreshold;
  }
  
  public boolean isViolation(int xTime, double yObserved) {
    checkArgument(xTime > 0);
    if(upperThreshold.containsKey(xTime)) {
      double upper = upperThreshold.get(xTime);
      double lower = lowerThreshold.get(xTime);
      return yObserved > upper || yObserved < lower;
    }
    else 
      return false;
  }
  
  public List<Range<Integer>> findThresholdViolations(int[] xs, double[] yObserved) {
    int maxPredictions = Math.min(xs.length, upperThreshold.size());
    LinkedList<Range<Integer>> violations = new LinkedList<>();
    boolean ongoingViolation = false;
    int violationStart = 0;
    for(int i = 1; i < maxPredictions; i++) {
      if(!ongoingViolation && isViolation(xs[i], yObserved[i])) {
        violationStart = xs[i];
        ongoingViolation = true;
      } else if(ongoingViolation && !isViolation(xs[i], yObserved[i])) {
        violations.addLast(Range.closedOpen(violationStart, xs[i]));
        ongoingViolation = false;
      } 
    }
    return violations;
  }
  
  public XYSeriesCollection getSeries() {
    XYSeriesCollection seriesCol = new XYSeriesCollection();
    XYSeries upperAlarmSeries = new XYSeries("Upper threshold");
    XYSeries lowerAlarmSeries = new XYSeries("Lower threshold");
    
    for(int key : upperThreshold.keySet()) {
      upperAlarmSeries.add(key, upperThreshold.get(key));
      lowerAlarmSeries.add(key, lowerThreshold.get(key));
    }
    seriesCol.addSeries(upperAlarmSeries);
    seriesCol.addSeries(lowerAlarmSeries);
    return seriesCol;
  }
}
