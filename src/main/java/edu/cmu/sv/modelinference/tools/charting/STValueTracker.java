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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.cmu.sv.modelinference.generators.LogProcessor;
import edu.cmu.sv.modelinference.generators.parser.reader.LogReader;
import edu.cmu.sv.modelinference.generators.parser.st.Coord3d;
import edu.cmu.sv.modelinference.generators.parser.st.STEntry;

public class STValueTracker implements LogProcessor<STEntry> {

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
  
  private Map<String, Map<FIELD, XYSeries>> field2callsign2series = new HashMap<>();
  private final Set<FIELD> fields;
  private final LogReader<STEntry> logReader;
  
  public STValueTracker(Set<FIELD> fields, LogReader<STEntry> logReader) {
    this.fields = fields;
    this.logReader = logReader;
    
    this.logReader.addLogProcessor(this);
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
  public void process(STEntry entry) {
    double timestamp = entry.getLogTime();
    Map<FIELD, XYSeries> f2xy = this.field2callsign2series.get(entry.getCallSign());
    if(f2xy == null) {
      f2xy = new HashMap<>();
      this.field2callsign2series.put(entry.getCallSign(), f2xy);
    }
    for(FIELD f : fields) {
      XYSeries ser = f2xy.get(f);
      if(ser == null) {
        ser = new XYSeries(f.id + "_" + entry.getCallSign());
        f2xy.put(f, ser);
      }
      double val = getValue(entry, f);
      ser.add(timestamp, val);
    }
  }
  
  public XYSeriesCollection getSeries(String logFile) throws IOException {
    this.logReader.parseLog(logFile);
    
    XYSeriesCollection col = new XYSeriesCollection();
    for(Map<FIELD, XYSeries> f2s : this.field2callsign2series.values()) {
      for(XYSeries ser : f2s.values()) {
        col.addSeries(ser);        
      }
    }
    return col;
  }

}
