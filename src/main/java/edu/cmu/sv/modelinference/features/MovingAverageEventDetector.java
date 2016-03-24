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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Kasper Luckow
 *
 */
public class MovingAverageEventDetector implements EventDetector {  
  
  private final int windowSize;
  private final double stdDevs;
  private static final int UNBOUNDED_WINDOW_SIZE = -1;
  private static final int DEFAULT_ALARM_MULTIPLIER = 3;
  
  public MovingAverageEventDetector() {
    this(UNBOUNDED_WINDOW_SIZE, DEFAULT_ALARM_MULTIPLIER);
  }
  
  public MovingAverageEventDetector(double stdDevsForAlarm) {
    this(UNBOUNDED_WINDOW_SIZE, stdDevsForAlarm);
  }
  
  public MovingAverageEventDetector(int windowSize, double stdDevsForAlarm) {
    checkArgument(stdDevsForAlarm > 0);
    
    this.windowSize = windowSize;
    this.stdDevs = stdDevsForAlarm;
  }
  
  @Override
  public PredictionModel computePredictionModel(int[] xs, double[] ys) {
    checkArgument(xs.length == ys.length);
    checkArgument(xs.length > 0);
    
    Map<Integer, Double> upperThreshold = new HashMap<>();
    Map<Integer, Double> lowerThreshold = new HashMap<>();
    
    LinkedList<Double> movingDataSet = new LinkedList<>();
    for(int i = 0; i < xs.length; i++) {
      if(this.windowSize > UNBOUNDED_WINDOW_SIZE && i > this.windowSize) {
        movingDataSet.removeLast();
      }
      movingDataSet.addFirst(ys[i]);
      double mean = computeMean(movingDataSet);
      double stdDev = Math.sqrt(computeVariance(movingDataSet, mean));
      
      //+1 here for forecasting
      upperThreshold.put(xs[i] + 1, mean + (stdDev * stdDevs));
      lowerThreshold.put(xs[i] + 1, mean - (stdDev * stdDevs));
    }
    
    return new PredictionModel(upperThreshold, lowerThreshold);
  }
  
  public double[] computeMovingAverage(double[] data) {
    LinkedList<Double> movingDataSet = new LinkedList<>();
    double[] movingAvg = new double[data.length];
    for(int i = 0; i < data.length; i++) {
      if(this.windowSize > UNBOUNDED_WINDOW_SIZE && i > this.windowSize) {
        movingDataSet.removeLast();
      }
      movingDataSet.addFirst(data[i]);
      double mean = computeMean(movingDataSet);
      movingAvg[i] = mean;
    }
    return movingAvg;
  }
  
  private double computeMean(List<Double> data) {
    double mean = 0;
    for(double d : data) {
      mean += d;
    }
    return mean / (double)data.size();
  }
  
  private static double computeVariance(Queue<Double> data, double mean) {
    double sqDev = 0;
    for(double d : data) {
      sqDev += Math.pow(d - mean, 2);
    }
    
    //It does not seem clear whether we should divide by the data size
    //or data size - 1
    return sqDev / (double)(data.size() - 1);
  }  
}
