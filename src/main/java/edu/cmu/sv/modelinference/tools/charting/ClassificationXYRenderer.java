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

import edu.cmu.sv.modelinference.features.classification.ClassificationResult;
import edu.cmu.sv.modelinference.features.classification.Event;
import edu.cmu.sv.modelinference.features.classification.EventClass;

/**
 * @author Kasper Luckow
 *
 */
public class ClassificationXYRenderer extends XYLineAndShapeRenderer {
  
  private static final long serialVersionUID = 155589501163174854L;
  
  //ugly
  private static Color[] COLORS = {Color.GREEN, Color.RED, Color.BLACK, Color.BLUE, Color.GRAY, Color.CYAN, Color.DARK_GRAY, Color.MAGENTA}; 
  
  private final ClassificationResult clusterResults;
  private static final Random rng = new Random();
  
  private Map<String, Color> clusterId2Color = new HashMap<>();
  private RangeMap<Integer, String> eventRanges2ClusterId = TreeRangeMap.create();
  
  public ClassificationXYRenderer(ClassificationResult clusterResults) {
    this.clusterResults = clusterResults;
    assignClusterColors(this.clusterResults);
    populateIndex(this.clusterResults);
  }

  private void populateIndex(ClassificationResult results) {
    for(EventClass cl : results.getEventClasses()) {
      for(Event evt : cl.getEvents()) {
        eventRanges2ClusterId.put(evt.getRange(), cl.getClassId());
      }
    }
  }
  
  private void assignClusterColors(ClassificationResult results) {
    int colorIdx = 0;
    int usedColors = 0;
    for(EventClass cl : results.getEventClasses()) {
      if(usedColors >= COLORS.length) {
        colorIdx = rng.nextInt(COLORS.length);
      }
      clusterId2Color.put(cl.getClassId(), COLORS[colorIdx++]);
      usedColors++;
    }
  }
  
  @Override
  public Paint getItemPaint(int row, int col) {
    String clusterId = eventRanges2ClusterId.get(col + 10);
    if(clusterId != null) {
      return clusterId2Color.get(clusterId);
    } else 
      return super.getItemPaint(row, col);
  }
}
