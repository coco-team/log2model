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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.Main;
import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.cmdutil.Util;
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
import edu.cmu.sv.modelinference.tools.model.Log2Model;

/**
 * @author Kasper Luckow
 */
public class Log2EventChart implements LogHandler<Void> {
  private static final Logger logger = LoggerFactory.getLogger(Log2EventChart.class.getName());

  private static final String CLUSTERS_ARG = "clusters";
  private static final String ALARM_ARG = "alarm";
  private static final String MOVING_AVG_DETECT_SIZE_ARG = "mad";
  private static final String MOVING_AVG_FEAT_SIZE_ARG = "maf";
  private static final String HELP_ARG = "help";
  
  private static final int DEFAULT_CLUSTERS = 6;
  private static final int DEFAULT_ALARM_LEVEL = 3;
  private static final int DEFAULT_DETECT_WINDOWSIZE = 2;
  private static final int DEFAULT_FEAT_WINDOWSIZE = 3;
  
  private Options cmdOpts;
  private int clusterNum, alarmSize, movingAvgDetection, movingAvgSizeFeat;
  
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

  private Log2EventChart() {
    this.cmdOpts = createCmdOptions();
  }

  @Override
  public String getHandlerName() {
    return "eventchart";
  }
 
  private Options createCmdOptions() {
    Options options = new Options();
    
    Option help = new Option(HELP_ARG, "print this message");    
    Option clusters = Option.builder(CLUSTERS_ARG).argName("number").hasArg()
        .desc("Specify number of clusters/event classes. Default is " + DEFAULT_CLUSTERS).build();
    Option alarm = Option.builder(ALARM_ARG).argName("number").hasArg()
        .desc("Specify size of upper and lower control limits in terms of number of std dev from expected val. Default is " + DEFAULT_ALARM_LEVEL).build();
    Option mvRaw = Option.builder(MOVING_AVG_DETECT_SIZE_ARG).argName("number").hasArg()
        .desc("Specify size of moving average for event detection. Default is " + DEFAULT_DETECT_WINDOWSIZE).build();
    Option mvFeat = Option.builder(MOVING_AVG_FEAT_SIZE_ARG).argName("number").hasArg()
        .desc("Specify size of moving average for smoothing feat data. Default is " + DEFAULT_FEAT_WINDOWSIZE).build();
    options.addOption(clusters);
    options.addOption(alarm);
    options.addOption(mvRaw);
    options.addOption(mvFeat);
    options.addOption(help);
    return options;
  }
  
  @Override
  public Void process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(cmdOpts, additionalCmdArgs, false);
    } catch(ParseException exp) {
      logger.error(exp.getMessage());
      System.err.println(exp.getMessage());
      Util.printHelpAndExit(Log2EventChart.class, cmdOpts);
    }
    
    if(cmd.hasOption(HELP_ARG))
      Util.printHelpAndExit(Log2EventChart.class, cmdOpts, 0);
    
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
      System.err.println("Supported log handlers: " + sb.toString());
      Util.printHelpAndExit(Log2EventChart.class, cmdOpts);
    }
    logger.info("Using loghandler for logtype: " + logHandler.getHandlerName());
    
    this.clusterNum = (cmd.hasOption(CLUSTERS_ARG)) ? Integer.parseInt(cmd.getOptionValue(CLUSTERS_ARG)) : DEFAULT_CLUSTERS;
    this.alarmSize = (cmd.hasOption(ALARM_ARG)) ? Integer.parseInt(cmd.getOptionValue(ALARM_ARG)) : DEFAULT_ALARM_LEVEL;
    this.movingAvgDetection = (cmd.hasOption(MOVING_AVG_DETECT_SIZE_ARG)) ? Integer.parseInt(cmd.getOptionValue(MOVING_AVG_DETECT_SIZE_ARG)) : DEFAULT_DETECT_WINDOWSIZE;
    this.movingAvgSizeFeat = (cmd.hasOption(MOVING_AVG_FEAT_SIZE_ARG)) ? Integer.parseInt(cmd.getOptionValue(MOVING_AVG_FEAT_SIZE_ARG)) : DEFAULT_FEAT_WINDOWSIZE;
    
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
    EventDetector movingAvg = new MovingAverageEventDetector(this.movingAvgDetection, this.alarmSize);

    EventGenerator eventGenerator = new AvgEventGenerator();
    
    EventClassifier classifier = new Clusterer1D(this.clusterNum, 100, 3000);
    
    EventVisualizer.Builder bldr = new EventVisualizer.Builder(movingAvg,
              slopeExtractor, eventGenerator, classifier);
    
    EWMASmoothingFilter ewmaFilter = new EWMASmoothingFilter(this.movingAvgSizeFeat, 0.1);
    EWMASmoothingFilter ewmaFilterFeature = new EWMASmoothingFilter(this.movingAvgSizeFeat, 0.1);
    EventVisualizer eventVisualizer = bldr.addRawDataSmoothingFilter(ewmaFilter)
        .addFeatureSmoothingFilter(ewmaFilterFeature)
        .build();
    
    eventVisualizer.visualize(producer, rawData);
  }
}
