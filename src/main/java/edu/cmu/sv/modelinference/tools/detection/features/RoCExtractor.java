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
package edu.cmu.sv.modelinference.tools.detection.features;

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
      
      //FIXME: This is an important one: de we want to compute the slope by also considering
      // the future i.e. upper = i + numPointsSeparator?
      // We did this previously, but then event can be incorrectly be found before they are
      // happening in the raw data.
      int upper = i;
      // upper = (upper >= yRaw.length) ? yRaw.length - 1 : upper;
      
      //FIXME: Not sure if we should divide by the interval as in the uncommented
      //part here or simply number of points regardless of time span
      //ySlope[i] = (yUpper - yLower) / (xRaw[upper] - xRaw[lower]);
      ySlope[i] = (yRaw[upper] - yRaw[lower]) / (numPointsSeparator + 1);
    }
    
    return ySlope;
  }
}
