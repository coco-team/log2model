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
package edu.cmu.sv.modelinference.tools.charting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kasper Luckow
 *
 */
public class DataPointCollection extends ArrayList<DataPoint> {
  private static final long serialVersionUID = 11241155232L;
  
  public double[][] toDataArray() {
    double[][] data = new double[][] {new double[this.size()], new double[this.size()]};
    int i = 0;
    for(DataPoint d : this) {
      data[0][i] = d.x;
      data[1][i++] = d.y;
    }
    return data;
  }
}
