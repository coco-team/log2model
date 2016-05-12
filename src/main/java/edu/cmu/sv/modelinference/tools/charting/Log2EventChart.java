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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.Log2Model;
import edu.cmu.sv.modelinference.Main;
import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.eventdetection.AvgEventGenerator;
import edu.cmu.sv.modelinference.tools.eventdetection.EWMASmoothingFilter;
import edu.cmu.sv.modelinference.tools.eventdetection.EventDetector;
import edu.cmu.sv.modelinference.tools.eventdetection.EventGenerator;
import edu.cmu.sv.modelinference.tools.eventdetection.EventVisualizer;
import edu.cmu.sv.modelinference.tools.eventdetection.FeatureExtractor;
import edu.cmu.sv.modelinference.tools.eventdetection.MovingAverageEventDetector;
import edu.cmu.sv.modelinference.tools.eventdetection.RoCExtractor;
import edu.cmu.sv.modelinference.tools.eventdetection.classification.Clusterer1D;
import edu.cmu.sv.modelinference.tools.eventdetection.classification.EventClassifier;

/**
 * @author Kasper Luckow
 */
public class Log2EventChart implements LogHandler<Void> {
  private static final Logger logger = LoggerFactory.getLogger(Log2EventChart.class.getName());

  private static Log2EventChart instance = null;
  
  public static Log2EventChart getInstance() {
    if(instance == null) {
      instance = new Log2EventChart();
    }
    return instance;
  }

  private static Set<LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>>> logHandlers = new HashSet<>();
  
  static {
    logHandlers.add(AREventChartHandler.getInstance());
    logHandlers.add(STEventChartHandler.getInstance());
  }
  
  private Log2EventChart() { }

  @Override
  public String getHandlerName() {
    return "eventchart";
  }
  
  @Override
  public Void process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>> logHandler = null;
    boolean found = false;
    for(LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>> lh : logHandlers) {
      if(lh.getHandlerName().equals(logType)) {
        logHandler = lh;
        found = true;
        break;
      }
    }
    if(!found) {
      StringBuilder sb = new StringBuilder();
      Iterator<LogHandler<ValueTrackerProducer<?, DataPointCollection, ?>>> logIter = logHandlers.iterator();
      while(logIter.hasNext()) {
        sb.append(logIter.next().getHandlerName());
        if(logIter.hasNext())
          sb.append(", ");
      }
      logger.error("Did not find loghandler for " + logType);
      throw new LogProcessingException("Supported log handlers: " + sb.toString());
    }
    logger.info("Using loghandler for logtype: " + logHandler.getHandlerName());
    
    ValueTrackerProducer<?, DataPointCollection, ?> valueExtractor = logHandler.process(logFile, logType, additionalCmdArgs);
    
    Map<String, DataPointCollection> rawData;
    try {
      rawData = valueExtractor.computeDataSet(new File(logFile));
    } catch (IOException e) {
      throw new LogProcessingException(e);
    }
    
    for(Entry<String, DataPointCollection> producer : rawData.entrySet()) {      
      performAnalysis(producer.getKey(), producer.getValue().toDataArray());
    }
    
    return null;
  }
  
  private void performAnalysis(String producer, double[][] rawData) {

    //Feature: slope/rate-of-change
    FeatureExtractor slopeExtractor = new RoCExtractor();
    
    //use event detector on features
    EventDetector movingAvg = new MovingAverageEventDetector(2, 3);

    EventGenerator eventGenerator = new AvgEventGenerator();
    
    EventClassifier classifier = new Clusterer1D(6, 100, 3000);
    
    EventVisualizer.Builder bldr = new EventVisualizer.Builder(movingAvg,
              slopeExtractor, eventGenerator, classifier);
    
    EWMASmoothingFilter ewmaFilter = new EWMASmoothingFilter(3, 0.1);
    EWMASmoothingFilter ewmaFilterFeature = new EWMASmoothingFilter(3, 0.1);
    EventVisualizer eventVisualizer = bldr.addRawDataSmoothingFilter(ewmaFilter)
        .addFeatureSmoothingFilter(ewmaFilterFeature)
        .build();
    
    eventVisualizer.visualize(producer, rawData);
  }
}
