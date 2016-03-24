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
import java.util.List;

import com.google.common.collect.Range;

/**
 * @author Kasper Luckow
 *
 */
public class AvgFeature implements Feature {

  public static List<Event> computeAvgEvents(List<Range<Integer>> eventSequence, double[] ys) {
    List<Event> avgForEvents = new ArrayList<>();
    int idx = 0;
    for(Range<Integer> event : eventSequence) {
      double[] yData = new double[event.upperEndpoint() - event.lowerEndpoint()];
      for(int i = 0, r = event.lowerEndpoint(); r < event.upperEndpoint(); r++, i++, idx++) {
        yData[i] = ys[idx];
      }
      avgForEvents.add(new Event(event, new AvgFeature(yData)));
    }    
    return avgForEvents;
  }
  
  private final double avg;
  
  public AvgFeature(double avg) {
    this.avg = avg;
  }
  
  public AvgFeature(double[] dataPoints) {
    this(avg(dataPoints));
  }
  
  public static double avg(double[] data) {
    double sum = 0.0;
    for(double d : data) {
      sum += d;
    }
    return sum / (double)data.length;
  }
  
  @Override
  public double getData() {
    return avg;
  }
}
