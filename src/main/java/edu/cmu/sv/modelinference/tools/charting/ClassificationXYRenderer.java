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

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;

import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import edu.cmu.sv.modelinference.tools.detection.features.classification.ClassificationResult;
import edu.cmu.sv.modelinference.tools.detection.features.classification.Event;
import edu.cmu.sv.modelinference.tools.detection.features.classification.EventClass;

/**
 * @author Kasper Luckow
 *
 */
public class ClassificationXYRenderer extends XYLineAndShapeRenderer {
  
  private static final long serialVersionUID = 155589501163174854L;

  private final ClassificationResult clusterResults;
  
  private Map<EventClass, Color> cluster2Color;
  private RangeMap<Integer, EventClass> eventRanges2Class = TreeRangeMap.create();
  
  private final int stepSize;
  
  public ClassificationXYRenderer(ClassificationResult clusterResults, int stepSize, Map<EventClass, Color> cluster2Color) {
    this.clusterResults = clusterResults;
    this.stepSize = stepSize;
    this.cluster2Color = cluster2Color;
    populateIndex(this.clusterResults);
  }

  private void populateIndex(ClassificationResult results) {
    for(EventClass cl : results.getEventClasses()) {
      for(Event evt : cl.getEvents()) {
        eventRanges2Class.put(evt.getRange(), cl);
      }
    }
  }
  
  @Override
  public Paint getItemPaint(int row, int col) {
    EventClass cluster = eventRanges2Class.get(col*stepSize + eventRanges2Class.span().lowerEndpoint() - 1);
    if(cluster != null) {
      Color clr = cluster2Color.get(cluster);
      return clr;
    } else 
      return super.getItemPaint(row, col);
  }
}
