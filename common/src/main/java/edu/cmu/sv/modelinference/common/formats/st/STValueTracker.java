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
package edu.cmu.sv.modelinference.common.formats.st;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.sv.modelinference.common.formats.DataPoint;
import edu.cmu.sv.modelinference.common.formats.DataPointCollection;
import edu.cmu.sv.modelinference.common.formats.DataSetFactory;
import edu.cmu.sv.modelinference.common.formats.ValueTrackerProducer;
import edu.cmu.sv.modelinference.common.parser.LogReader;

/**
 * 
 * @author Kasper Luckow
 *
 * This class should be removed and made generic instead. Instead of a having a notion of "field" only for
 * ST, this should be generalized in which case we can use Data points generator for arbitrary log formats.
 */
public abstract class STValueTracker<S> extends ValueTrackerProducer<STEntry, S, Double> {
  
  public static class STDataPointsGenerator extends STValueTracker<DataPointCollection> {
    private static final DataSetFactory<DataPointCollection> FACTORY = new DataSetFactory<DataPointCollection>() {
      @Override
      public DataPointCollection create(String producer) {
        return new DataPointCollection();
      }
    };
    
    public STDataPointsGenerator(STValueTracker.FIELD field,
        LogReader<STEntry> logReader) {
      super(field, logReader, FACTORY);
    }

    @Override
    public void addToDataSet(DataPointCollection dataset, double time, Double data) {
      dataset.add(new DataPoint(time, data));
    }
  }
  
  //This is really ugly -- should have a tighter connection to the entry
  //Currently only supports numeric types. It would be nice to also track string types e.g. AC_TYPE
  public static enum FIELD {
    PHI("phi", "NA"),
    SPEED("speed", "km/h"),
    UTCTIME("utc", "s"),
    POS_X("x", "feet"),
    POS_Y("y", "feet"),
    POS_Z("z", "feet");
    
    private final String id;
    private final String unit;
    
    public String getId() {
      return this.id;
    }
    
    public String getUnit() {
      return this.unit;
    }
    
    FIELD(String id, String unit) {
      this.id = id;
      this.unit = unit;
    }
  }
  
  private final FIELD field;
  
  public STValueTracker(FIELD field, LogReader<STEntry> logReader, DataSetFactory<S> dataFactory) {
    super(logReader, dataFactory);
    this.field = field;
  }
  
  private double getValue(STEntry entry, FIELD f) {
    switch(f) {
    case PHI:
      return entry.getPhi();
    case SPEED:
      return entry.getSpeed();
    case UTCTIME:
      return entry.getUtcTime();
    case POS_X:
      return entry.getPosition().getX();
    case POS_Y:
      return entry.getPosition().getY();
    case POS_Z:
      return entry.getPosition().getZ();
    default:
      throw new RuntimeException("Does not support field type " + f.id);
    }
  }

  @Override
  public Double getData(STEntry entry) {
    double val = getValue(entry, field);
    return val;
  }
}
