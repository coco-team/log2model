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

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import edu.cmu.sv.modelinference.features.AvgEventGenerator;
import edu.cmu.sv.modelinference.features.EWMASmoothingFilter;
import edu.cmu.sv.modelinference.features.EventDetector;
import edu.cmu.sv.modelinference.features.EventGenerator;
import edu.cmu.sv.modelinference.features.EventVisualizer;
import edu.cmu.sv.modelinference.features.FeatureExtractor;
import edu.cmu.sv.modelinference.features.MovingAverageEventDetector;
import edu.cmu.sv.modelinference.features.PredictionModel;
import edu.cmu.sv.modelinference.features.RoCExtractor;
import edu.cmu.sv.modelinference.features.UnweightedRectangularSmoothingFilter;
import edu.cmu.sv.modelinference.features.classification.AvgFeature;
import edu.cmu.sv.modelinference.features.classification.ClassificationResult;
import edu.cmu.sv.modelinference.features.classification.Clusterer1D;
import edu.cmu.sv.modelinference.features.classification.Event;
import edu.cmu.sv.modelinference.features.classification.EventClass;
import edu.cmu.sv.modelinference.features.classification.EventClassifier;
import edu.cmu.sv.modelinference.features.classification.EventUtils;
import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;
import edu.cmu.sv.modelinference.generators.parser.reader.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.parser.st.STEntry;
import edu.cmu.sv.modelinference.generators.parser.st.STParser;
import edu.cmu.sv.modelinference.tools.charting.STValueTracker.FIELD;

/**
 * @author Kasper Luckow
 */
public class Log2EventChart {
  private static final String HELP_ARG = "help";
  private static final String INPUT_ARG = "i";
  private static final String INPUT_TYPE_ARG = "t";
  private static final String ADD_OPTS_ARG = "a";
  
  private static final Logger logger = LoggerFactory.getLogger(Log2EventChart.class.getName());
      
  
  public static void main(String[] args) throws IOException {    
    Options options = createCmdOptions();
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch(ParseException exp) {
      printHelpAndExit(options);
    }
    
    if(cmd.hasOption(HELP_ARG)) {
      printHelpAndExit(options);
    }
    
    String logFilePath = null;
    if(cmd.hasOption(INPUT_ARG)) {
      logFilePath = cmd.getOptionValue(INPUT_ARG);
    } else {
      printHelpAndExit(options);
    }
    
    //use enums instead
    String logType = "";
    
    //When options are required (required() called in option builder), do
    //we need to check hasValue, or will the parser simply throw an exception
    //if the option is not provided?
    if(cmd.hasOption(INPUT_TYPE_ARG)) {
      logType = cmd.getOptionValue(INPUT_TYPE_ARG);
    } else
      printHelpAndExit(options);
    
    long start = System.currentTimeMillis();
    
    ValueTrackerProducer<?, DataPointCollection, ?> valueExtractor = null;
    
    //Expand this to be more pluggable. ugly
    switch(logType.toLowerCase()) {
    case "st":
      FIELD trackedField =null;
      try {
        trackedField = FIELD.valueOf(cmd.getOptionValue(ADD_OPTS_ARG).toUpperCase());
      } catch(Exception e) {
        logger.error(e.getMessage());
        logger.error("Must be supplied a field to be tracked (e.g., pos_x) to additional arg option");
        System.exit(-1);
      }
      
      // Hardcoded atm :/
      LogEntryFilter<STEntry> filter =
      new LogEntryFilter<STEntry>() {
        String tracked = "";
        @Override
        public boolean submitForProcessing(STEntry entry) {
          if(entry.getCallSign().equals("USA5596")) {
            tracked = entry.getCallSign();
            return true;
          }          
          return entry.getCallSign().equals(tracked);
        }
      };
      
      LogReader<STEntry> reader = new SequentialLogReader<>(new STParser(), filter);
      
      valueExtractor = new STValueTracker.STDataPointsGenerator(trackedField, reader);
      break;
    case "autoresolver":
    case "rp":
      default:
        logger.error("Unsupported input type");
        printHelpAndExit(options);
    }
    
    
    Map<String, DataPointCollection> rawData = valueExtractor.computeDataSet(logFilePath);
    
    for(Entry<String, DataPointCollection> producer : rawData.entrySet()) {      
      performAnalysis(producer.getKey(), producer.getValue().toDataArray());
    }
    
    logger.info("Done constructing chart");
    logger.info("Processing time: " + (System.currentTimeMillis() - start));
  }
  
  private static void performAnalysis(String producer, double[][] rawData) {

    //Feature: slope/rate-of-change
    FeatureExtractor slopeExtractor = new RoCExtractor();
    
    //use event detector on features
    EventDetector movingAvg = new MovingAverageEventDetector(2, 3);

    EventGenerator eventGenerator = new AvgEventGenerator();
    
    EventClassifier classifier = new Clusterer1D(7, -1, 3000);
    
    EventVisualizer.Builder bldr = new EventVisualizer.Builder(movingAvg,
              slopeExtractor, eventGenerator, classifier);
    
    EWMASmoothingFilter ewmaFilter = new EWMASmoothingFilter(3, 0.1);
    EWMASmoothingFilter ewmaFilterFeature = new EWMASmoothingFilter(3, 0.1);
    EventVisualizer eventVisualizer = bldr.addRawDataSmoothingFilter(ewmaFilter)
        .addFeatureSmoothingFilter(ewmaFilterFeature)
        .build();
    
    eventVisualizer.visualize(producer, rawData);
  }
  
  private static void printHelpAndExit(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(Log2EventChart.class.getName(), options);
    System.exit(0);
  }
  
  public static Options createCmdOptions() {
    Options options = new Options();
    
    Option help = new Option(HELP_ARG, "print this message");

    Option inputType = Option.builder(INPUT_TYPE_ARG).argName("st | autoresolver")
        .hasArg()
        .desc("Specify log type")
        .required()
        .build();
    
    Option addOpts = Option.builder(ADD_OPTS_ARG).argName("Additional options").hasArg()
        .desc("Additional input type options").build();
    
    Option input = Option.builder(INPUT_ARG).argName("file")
                                .hasArg()
                                .desc("Specify input file.")
                                .required()
                                .build();


    options.addOption(inputType);
    options.addOption(addOpts);
    options.addOption(help);
    options.addOption(input);
    return options;
  }
}
