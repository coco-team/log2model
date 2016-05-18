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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Range;

/**
 * @author Kasper Luckow
 *
 */
public class EventUtils {

  public static List<Range<Integer>> computeEventSequence(int[] xs, List<Range<Integer>> violations) {
    return computeEventSequence(xs[0], xs[xs.length - 1], violations);
  }
  
  public static List<Range<Integer>> computeEventSequence(double minX, double maxX, List<Range<Integer>> violations) {
    LinkedList<Range<Integer>> eventSequence = new LinkedList<>();
    int begin = (int)minX;
    for(Range<Integer> violation : violations) {
      int endpoint = violation.lowerEndpoint();
      eventSequence.add(Range.closedOpen(begin, endpoint));
      begin = endpoint;      
    }
    eventSequence.add(Range.closedOpen(begin, (int)maxX));
    return eventSequence;
  }
}
