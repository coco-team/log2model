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

/**
 * @author Kasper Luckow
 *
 */
public class RoCExtractor implements FeatureExtractor {

  private final int numPointsSeparator;
  
  public RoCExtractor() {
    this(1);
  }
  
  public RoCExtractor(int numPointsSeparator) {
    this.numPointsSeparator = numPointsSeparator;
  }
  
  @Override
  public double[] computeFeature(double[] xRaw, double[] yRaw) {
    double[] ySlope = new double[yRaw.length];
    
    for(int i = 0; i < yRaw.length; i++) {
      int lower = i - numPointsSeparator;
      lower = (lower < 0) ? 0 : lower;
      int upper = i + numPointsSeparator;
      upper = (upper >= yRaw.length) ? yRaw.length - 1 : upper;
      
      ySlope[i] = (yRaw[upper] - yRaw[lower]) / (xRaw[upper] - xRaw[lower]);
    }
    
    return ySlope;
  }
}
