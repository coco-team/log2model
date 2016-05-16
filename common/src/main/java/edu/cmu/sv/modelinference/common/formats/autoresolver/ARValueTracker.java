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
package edu.cmu.sv.modelinference.common.formats.autoresolver;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeries;

import edu.cmu.sv.modelinference.generators.DataSetFactory;
import edu.cmu.sv.modelinference.generators.ValueTrackerProducer;
import edu.cmu.sv.modelinference.generators.formats.st.STEntry;
import edu.cmu.sv.modelinference.generators.parser.LogReader;
import edu.cmu.sv.modelinference.tools.charting.DataPoint;
import edu.cmu.sv.modelinference.tools.charting.DataPointCollection;

/**
 * 
 * @author Kasper Luckow
 *
 * This class should be removed and made generic instead. Instead of a having a notion of "field" only for
 * ST, this should be generalized in which case we can use Data points generator for arbitrary log formats.
 */
public abstract class ARValueTracker<S> extends ValueTrackerProducer<AutoresolverEntry, S, Double> {
  
  public static class ARValueSeriesGenerator extends ARValueTracker<XYSeries> {
    private static final DataSetFactory<XYSeries> FACTORY = new DataSetFactory<XYSeries>() {
      @Override
      public XYSeries create(String producer) {
        return new XYSeries(producer);
      }
    };
    
    public ARValueSeriesGenerator(edu.cmu.sv.modelinference.generators.formats.autoresolver.ARValueTracker.FIELD field,
        LogReader<AutoresolverEntry> logReader) {
      super(field, logReader, FACTORY);
    }

    @Override
    public void addToDataSet(XYSeries dataset, double time, Double data) {
      dataset.add(time, data);
    }
  }
  
  public static class ARDataPointsGenerator extends ARValueTracker<DataPointCollection> {
    private static final DataSetFactory<DataPointCollection> FACTORY = new DataSetFactory<DataPointCollection>() {
      @Override
      public DataPointCollection create(String producer) {
        return new DataPointCollection();
      }
    };
    
    public ARDataPointsGenerator(edu.cmu.sv.modelinference.generators.formats.autoresolver.ARValueTracker.FIELD field,
        LogReader<AutoresolverEntry> logReader) {
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
    ALT("alt", "ft"),
    ALT_RATE("alt_rate", "N/A"),
    FUEL_WEIGHT("fueld", "N/A"),
    GRND_SPEED("grnd_speed", "N/A"),
    HEADING("heading", "N/A"),
    IND_SPEED("indic_speed", "N/A"),
    LAT("lat", "dg"),
    LON("lon", "dg"),
    TRUE_AIRSP("t_airspeed", "N/A"),
    TRUE_COURSE("true_course", "N/A");
    
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
  
  public ARValueTracker(FIELD field, LogReader<AutoresolverEntry> logReader, DataSetFactory<S> dataFactory) {
    super(logReader, dataFactory);
    this.field = field;
  }
  
  private double getValue(AutoresolverEntry entry, FIELD f) {
    switch(f) {
    case ALT:
      return entry.getAltitude();
    case ALT_RATE:
      return entry.getAltitudeRateFpm();
    case FUEL_WEIGHT:
      return entry.getFuelWeight();
    case GRND_SPEED:
      return entry.getGroundSpeed();
    case HEADING:
      return entry.getHeading();
    case IND_SPEED:
      return entry.getIndicatedSpeed();
    case LAT:
      return entry.getLatDegrees();
    case LON:
      return entry.getLonDegrees();
    case TRUE_AIRSP:
      return entry.getTrueAirspeed();
    case TRUE_COURSE:
      return entry.getTrueCourse();
    default:
      throw new RuntimeException("Does not support field type " + f.id);
    }
  }

  @Override
  public Double getData(AutoresolverEntry entry) {
    double val = getValue(entry, field);
    return val;
  }
}
