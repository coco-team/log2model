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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import com.google.common.collect.Range;

/**
 * @author Kasper Luckow
 *
 */
public class Event {
  private final Range<Integer> xRange;
  private final Feature feat;
  
  public Event(Range<Integer> xRange, Feature feat) {
    this.xRange = xRange;
    this.feat = feat;
  }
  
  public Range<Integer> getRange() {
    return this.xRange;
  }
  
  public Feature getFeature() {
    return this.feat;
  }
}
