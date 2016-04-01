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
package edu.cmu.sv.modelinference.detection.features;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.util.Pair;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.google.common.collect.Range;

import edu.cmu.sv.modelinference.tools.charting.DataPoint;
import edu.cmu.sv.modelinference.tools.charting.DataPointCollection;

/**
 * @author Kasper Luckow
 *
 */

public class PredictionModel {
  private SortedMap<Integer, Double> upperThreshold;
  private SortedMap<Integer, Double> lowerThreshold;
  
  PredictionModel(SortedMap<Integer, Double> upperThreshold, SortedMap<Integer, Double> lowerThreshold) {
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
  
  public List<Range<Integer>> findThresholdViolations(double[] xs, double[] yObserved) {
    int maxPredictions = Math.min(xs.length, upperThreshold.size());
    LinkedList<Range<Integer>> violations = new LinkedList<>();
    boolean ongoingViolation = false;
    int violationStart = 0;
    for(int i = 1; i < maxPredictions; i++) {
      double x = xs[i];
      if(!ongoingViolation && isViolation((int)x, yObserved[i])) {
        violationStart = (int)xs[i];
        ongoingViolation = true;
      } else if(ongoingViolation && !isViolation((int)x, yObserved[i])) {
        violations.addLast(Range.closedOpen(violationStart, (int)x));
        ongoingViolation = false;
      } 
    }
    return violations;
  }
  
  public DataPointCollection getUpperThreshold() {
    return computeThreshold(upperThreshold);
  }
  
  public DataPointCollection getLowerThreshold() {
    return computeThreshold(lowerThreshold);
  }
  
  private static DataPointCollection computeThreshold(SortedMap<Integer, Double> threshold) {
    DataPointCollection dp = new DataPointCollection();
    for(int key : threshold.keySet()) {
      dp.add(new DataPoint(key, threshold.get(key)));
    }
    return dp;
  }
}
