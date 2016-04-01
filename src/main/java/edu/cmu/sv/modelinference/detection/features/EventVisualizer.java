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
package edu.cmu.sv.modelinference.detection.features;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import edu.cmu.sv.modelinference.detection.features.classification.ClassificationResult;
import edu.cmu.sv.modelinference.detection.features.classification.Event;
import edu.cmu.sv.modelinference.detection.features.classification.EventClass;
import edu.cmu.sv.modelinference.detection.features.classification.EventClassifier;
import edu.cmu.sv.modelinference.detection.features.classification.EventUtils;
import edu.cmu.sv.modelinference.tools.charting.ClassificationXYRenderer;
import edu.cmu.sv.modelinference.tools.charting.DataChart;
import edu.cmu.sv.modelinference.tools.charting.DataPointCollection;
import edu.cmu.sv.modelinference.tools.charting.Log2EventChart;

/**
 * @author Kasper Luckow
 *
 */
public class EventVisualizer {
  
  private static final Logger logger = LoggerFactory.getLogger(Log2EventChart.class.getName());

  private final EventDetector evtDetector;
  private final SmoothingFilter smoothingFilter;
  private final FeatureExtractor featureExtractor;
  private final SmoothingFilter featureSmoothingFilter;
  private final EventGenerator eventGenerator;
  private final EventClassifier eventClassifier;
  
  public static class Builder {
    private EventDetector evtDetector;
    private SmoothingFilter smoothingFilter = null;
    private FeatureExtractor featureExtractor;
    private SmoothingFilter featureSmoothingFilter = null;
    private EventGenerator eventGenerator;
    private EventClassifier eventClassifier;
    
    public Builder(EventDetector evtDetector, FeatureExtractor featureExtractor, EventGenerator eventGenerator, EventClassifier eventClassifier) {
      this.evtDetector = evtDetector;
      this.featureExtractor = featureExtractor;
      this.eventGenerator = eventGenerator;
      this.eventClassifier = eventClassifier;
    }
    
    public Builder addFeatureSmoothingFilter(SmoothingFilter filter) {
      this.featureSmoothingFilter = filter;
      return this;
    }
    
    public Builder addRawDataSmoothingFilter(SmoothingFilter filter) {
      this.smoothingFilter = filter;
      return this;
    }
    
    public EventVisualizer build() {
      return new EventVisualizer(evtDetector, smoothingFilter, 
          featureExtractor, featureSmoothingFilter, eventGenerator, eventClassifier);
    }
  }
  
  private EventVisualizer(EventDetector evtDetector, SmoothingFilter rawDataSmoothingFilter, FeatureExtractor featureExtractor, SmoothingFilter featureSmoothingFilter, EventGenerator eventGenerator, EventClassifier eventClassifier) {
    this.evtDetector = evtDetector;
    this.smoothingFilter = rawDataSmoothingFilter;
    this.featureExtractor = featureExtractor;
    this.featureSmoothingFilter = featureSmoothingFilter;
    this.eventGenerator = eventGenerator;
    this.eventClassifier = eventClassifier;
  }
  
  public void visualize(String producer, double[][] rawData) {
    //Compute all the series
    DefaultXYDataset featureDataSet = new DefaultXYDataset();
    DefaultXYDataset rawDataSet = new DefaultXYDataset();
    
    //Get features
    //Feature: raw data
    double[] rawXs = rawData[0];
    double[] rawYs = rawData[1];

    rawDataSet.addSeries(producer + " (raw)", rawData);
    
    //get time step size
    //the assumption here is that the step size is fixed!
    int timeStepSize = (int)(rawXs[rawXs.length - 1] - rawXs[0])/(rawXs.length-1);
    
    double[] ys;
    if(smoothingFilter != null) {
      ys = smoothingFilter.smoothen(rawXs, rawYs);
      featureDataSet.addSeries(producer + " (smooth)", new double[][] {rawXs, ys});
    } else {
      ys = rawYs;
    }
    
    double[] yFeatRaw = featureExtractor.computeFeature(rawXs, ys);
    featureDataSet.addSeries("Feature (raw)", new double[][] {rawXs, yFeatRaw});
    
    double[] yFeat;
    if(featureSmoothingFilter != null) {
      yFeat = featureSmoothingFilter.smoothen(rawXs, yFeatRaw);
      featureDataSet.addSeries("Feature (smooth)", new double[][] {rawXs, yFeat});
    } else {
      yFeat = yFeatRaw;
    }

    //Get prediction model based on moving average on slope
    PredictionModel predictionModel = evtDetector.computePredictionModel(rawXs, yFeat);
    DataPointCollection upperThreshold = predictionModel.getUpperThreshold();
    DataPointCollection lowerThreshold = predictionModel.getLowerThreshold();
    
    featureDataSet.addSeries("Upper thres", upperThreshold.toDataArray());
    featureDataSet.addSeries("Lower thres", lowerThreshold.toDataArray());    

//    
//    DataChart c = new DataChart("Featuresssssssssss chart");
//    JFreeChart chart = c.chart("");    
//
//    //Plot violations
//    XYPlot plot = chart.getXYPlot();
//    plot.setDataset(0, featureDataSet);
//    
//    c.pack();
//    RefineryUtilities.centerFrameOnScreen(c);
//    c.setVisible(true);    
    
    List<Range<Integer>> violations = predictionModel.findThresholdViolations(rawXs, yFeat);
    List<Range<Integer>> eventIntervals = EventUtils.computeEventSequence(rawXs[0], rawXs[rawXs.length - 1], violations);
    
    
    logger.info("Number of violations: " + violations.size());
    logger.info("Points in raw xs: " + rawXs.length);
    logger.info("Points in feature: " + yFeat.length);
    
    List<Event> events = eventGenerator.computeEvents(eventIntervals, yFeat, timeStepSize);
    logger.info("events computed: " + events.size());

    ClassificationResult classes = eventClassifier.classify(events);
    logger.info("Number of classes: " + classes.getEventClasses().size());
    Set<Map.Entry<Event, EventClass>> evtClassSequence = classes.getEvtSeqWithClassifiers();

    //Debugging
    DecimalFormat df = new DecimalFormat("#.0000"); 
    for(Map.Entry<Event, EventClass> evtWithClass : evtClassSequence) {
      Range<Integer> evtPeriod = evtWithClass.getKey().getRange();
      logger.info(evtPeriod + " : class " + 
          evtWithClass.getValue().getClassId() + 
          " ([" + df.format(evtWithClass.getValue().getMinFeatureVal()) + ";" + df.format(evtWithClass.getValue().getMaxFeatureVal()) + "], " +
          "avg: " + df.format(evtWithClass.getValue().getAvgFeatureVal()) + 
          ", stddev: " + df.format(Math.sqrt(evtWithClass.getValue().getFeatureVariance())) + ")");
    }
    
    Map<EventClass, Color> clusterColors = assignClassColors(classes);
    
    //Visualize classes
    visualizeClasses(classes, clusterColors);
    
    //Visualize features
    visualizeFeatures(classes, clusterColors, violations, timeStepSize, rawDataSet, featureDataSet);
  }
  
  private void visualizeClasses(ClassificationResult classes, Map<EventClass, Color> clusterColors) {
    DataChart clustersDataChart = new DataChart("Classification chart");      
    JFreeChart clusterChart = clustersDataChart.chart("");
    XYPlot clusterPlot = clusterChart.getXYPlot();
    
    int dataSetIndex = 0;
    Map<Integer, EventClass> dataSetIdx2EvtClass = new HashMap<>(); //ugly
    DefaultXYDataset eventDataSet = new DefaultXYDataset();
    for(EventClass evtCl : classes.getEventClasses()) {
      double[][] clusterDataSet = new double[][] {new double[evtCl.getEvents().size()], new double[evtCl.getEvents().size()]};
      int i = 0;
      for(Event data : evtCl.getEvents()) {
        clusterDataSet[0][i] = 0;
        clusterDataSet[1][i] = data.getFeature().getData();
        i++;
      }
      eventDataSet.addSeries("Class " + evtCl.getClassId(), clusterDataSet);

      dataSetIdx2EvtClass.put(dataSetIndex, evtCl);
      dataSetIndex++;
    }
    clusterPlot.setDataset(eventDataSet);

    for(Entry<Integer, EventClass> ent : dataSetIdx2EvtClass.entrySet()) {
      int idx = ent.getKey();
      Color clr = clusterColors.get(ent.getValue());
      logger.info("Setting color " + clr + " for event class " + ent.getValue().getClassId() + " with index " + idx);
      clusterPlot.getRendererForDataset(clusterPlot.getDataset(0)).setSeriesPaint(idx, clr); 
    }

    clustersDataChart.pack();
    RefineryUtilities.centerFrameOnScreen(clustersDataChart);
    clustersDataChart.setVisible(true);
  }
  
  private void visualizeFeatures(ClassificationResult classes, Map<EventClass, Color> clusterColors, List<Range<Integer>> violations, int timeStepSize, DefaultXYDataset rawDataSet, DefaultXYDataset featuresDataSet) {

    //Get chart on which we will plot the features and violations
    DataChart c = new DataChart("Features chart");
    JFreeChart chart = c.chart("");    

    //Plot violations
    XYPlot plot = chart.getXYPlot();
    plot.setDataset(0, rawDataSet);
    plot.setRenderer(0, new ClassificationXYRenderer(classes, timeStepSize, clusterColors));
    
    plot.setDataset(1, featuresDataSet);
    plot.setRenderer(1, new XYLineAndShapeRenderer());

    setViolationMarkers(violations, plot);
    c.pack();
    RefineryUtilities.centerFrameOnScreen(c);
    c.setVisible(true);
  }
  
  private static Map<EventClass, Color> assignClassColors(ClassificationResult results) {
    Color[] COLORS = {Color.GREEN, Color.RED, Color.BLACK, Color.BLUE, Color.GRAY, Color.CYAN, Color.DARK_GRAY, Color.MAGENTA}; 
    int colorIdx = 0;
    Map<EventClass, Color> cluster2Color = new HashMap<>();
    for(EventClass cl : results.getEventClasses()) {
      cluster2Color.put(cl, COLORS[colorIdx % COLORS.length]);
      colorIdx++;
    }
    return cluster2Color;
  }
  
  private void setViolationMarkers(Collection<Range<Integer>> violations, XYPlot plot) {
    for(Range<Integer> violation : violations) {
      
      ValueMarker startViolationMarker = new ValueMarker(violation.lowerEndpoint());  // position is the value on the axis
      startViolationMarker.setPaint(Color.BLACK);
      startViolationMarker.setStroke(new BasicStroke(1.0f));
      plot.addDomainMarker(startViolationMarker);
      
      ValueMarker endViolationMarker = new ValueMarker(violation.upperEndpoint());  // position is the value on the axis
      endViolationMarker.setPaint(Color.GREEN);
      endViolationMarker.setStroke(new BasicStroke(0.5f));
      plot.addDomainMarker(endViolationMarker);
    }
  }
}
