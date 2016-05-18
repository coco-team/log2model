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
package edu.cmu.sv.modelinference.eventtool;

import org.jfree.data.xy.XYSeries;

import edu.cmu.sv.modelinference.common.formats.DataSetFactory;
import edu.cmu.sv.modelinference.common.formats.autoresolver.ARValueTracker;
import edu.cmu.sv.modelinference.common.formats.autoresolver.AutoresolverEntry;
import edu.cmu.sv.modelinference.common.parser.LogReader;

public class ARValueSeriesGenerator extends ARValueTracker<XYSeries> {
    private static final DataSetFactory<XYSeries> FACTORY = new DataSetFactory<XYSeries>() {
      @Override
      public XYSeries create(String producer) {
        return new XYSeries(producer);
      }
    };
    
    public ARValueSeriesGenerator(ARValueTracker.FIELD field,
        LogReader<AutoresolverEntry> logReader) {
      super(field, logReader, FACTORY);
    }

    @Override
    public void addToDataSet(XYSeries dataset, double time, Double data) {
      dataset.add(time, data);
    }
  }
