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
package edu.cmu.sv.modelinference.common.formats.uas;

import edu.cmu.sv.modelinference.common.generators.LogParser;
import edu.cmu.sv.modelinference.common.generators.ParserException;

/**
 * @author Kasper Luckow
 *
 */
public class UASParser implements LogParser<UASEntry> {

  @Override
  public UASEntry parse(String rawEntry) throws ParserException {
    String[] entry = rawEntry.split("\\s+");
    if(entry.length != 175) //Not a valid entry (e.g. newline)
      throw new ParserException("Invalid entry length");
    try {      
      double time = Float.valueOf(entry[0]);
      double lat = Float.valueOf(entry[8]);
      double lon = Float.valueOf(entry[9]);
      double alt = Float.valueOf(entry[27]);
      double height = Float.valueOf(entry[10]);
      double speed = Float.valueOf(entry[14]);
      double direction = Float.valueOf(entry[15]);
      double roll = Float.valueOf(entry[38]);
      double pitch = Float.valueOf(entry[39]);
      double yaw = Float.valueOf(entry[40]);
      
      return new UASEntry(time, lat, lon, alt, height, speed, direction, roll, pitch, yaw);
    } catch (NumberFormatException e) { // Not a valid entry
      throw new ParserException(e);
    }
  }
}
