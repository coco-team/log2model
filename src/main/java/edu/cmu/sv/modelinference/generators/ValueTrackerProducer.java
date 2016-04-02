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
package edu.cmu.sv.modelinference.generators;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.formats.st.STValueTracker.FIELD;
import edu.cmu.sv.modelinference.generators.parser.LogReader;

/**
 * @author Kasper Luckow
 *
 */
public abstract class ValueTrackerProducer<T extends LogEntry, S, U> implements LogProcessor<T> {
  
  private Map<String, S> logproducer2dataset = new HashMap<>();
  private final LogReader<T> logReader;
  private final DataSetFactory<S> dataFactory;
  
  public ValueTrackerProducer(LogReader<T> logReader, DataSetFactory<S> dataFactory) {
    this.logReader = logReader;
    this.logReader.addLogProcessor(this);
    this.dataFactory = dataFactory;
  }

  public Map<String, S> computeDataSet(File logFile) throws IOException {
    this.logproducer2dataset = new HashMap<>();
    this.logReader.parseLog(logFile);
    return this.logproducer2dataset;
  }
  
  @Override
  public void process(T entry) {
    if(logproducer2dataset == null)
      logproducer2dataset = new HashMap<>();
    
    double timestamp = entry.getLogTime();
    S dataSet = this.logproducer2dataset.get(entry.getLogProducerId());
    if(dataSet == null) {
      dataSet = dataFactory.create(entry.getLogProducerId());
      this.logproducer2dataset.put(entry.getLogProducerId(), dataSet);
    }
    U data = getData(entry);
    addToDataSet(dataSet, timestamp, data);
  }
  
  public abstract U getData(T entry);
  
  public abstract void addToDataSet(S dataset, double time, U data);
}
