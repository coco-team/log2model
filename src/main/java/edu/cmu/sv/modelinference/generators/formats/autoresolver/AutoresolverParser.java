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
package edu.cmu.sv.modelinference.generators.formats.autoresolver;

import edu.cmu.sv.modelinference.generators.LogParser;
import edu.cmu.sv.modelinference.generators.ParserException;

/**
 * @author Kasper Luckow
 */
public class AutoresolverParser implements LogParser<AutoresolverEntry> {

  @Override
  public AutoresolverEntry parse(String raw) throws ParserException {
    String[] entry = raw.split(",");
    if(entry.length != 12) //Not a valid entry (e.g. newline)
      throw new ParserException("Invalid entry length");
    try {      
      double time = Double.valueOf(entry[0]);
      String flightName = entry[1];
      double altitude = Double.valueOf(entry[2]);
      double altitudeRateFpm = Double.valueOf(entry[3]);
      double fuelWeight = Double.valueOf(entry[4]);
      double groundSpeed = Double.valueOf(entry[5]);
      double heading = Double.valueOf(entry[6]);
      double indicatedSpeed = Double.valueOf(entry[7]);
      double latDegrees = Double.valueOf(entry[8]);
      double lonDegrees = Double.valueOf(entry[9]);
      double trueAirspeed = Double.valueOf(entry[10]);
      double trueCourse = Double.valueOf(entry[11]);
      return new AutoresolverEntry(time, flightName, altitude, altitudeRateFpm, fuelWeight, groundSpeed, heading, indicatedSpeed, latDegrees, lonDegrees, trueAirspeed, trueCourse);      
    } catch (NumberFormatException e) { // Not a valid entry
      throw new ParserException(e);
    }
  }
}
