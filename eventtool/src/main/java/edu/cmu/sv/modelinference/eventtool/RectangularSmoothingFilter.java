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
package edu.cmu.sv.modelinference.eventtool;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Kasper Luckow
 *
 */
public abstract class RectangularSmoothingFilter implements SmoothingFilter {
  
  private final int windowSize;
  private static final int UNBOUNDED_WINDOW_SIZE = -1;

  public RectangularSmoothingFilter(int windowSize) {
    this.windowSize = windowSize;
  }
  
  public RectangularSmoothingFilter() {
    this(UNBOUNDED_WINDOW_SIZE);
  }
  
  @Override
  public double[] smoothen(double[] xs, double[] ys) {
    LinkedList<Double> movingDataSet = new LinkedList<>();
    double[] movingAvg = new double[ys.length];
    for(int i = 0; i < ys.length; i++) {
      if(this.windowSize > UNBOUNDED_WINDOW_SIZE && i > this.windowSize) {
        movingDataSet.removeLast();
      }
      movingDataSet.addFirst(ys[i]);
      double mean = computeMean(movingDataSet);
      movingAvg[i] = mean;
    }
    return movingAvg;
  }
  
  protected abstract double computeMean(List<Double> data);
}
