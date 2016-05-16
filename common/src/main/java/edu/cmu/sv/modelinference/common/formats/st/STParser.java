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

import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.LogParser;
import edu.cmu.sv.modelinference.generators.ParserException;

/**
 * @author Kasper Luckow
 */
public class STParser implements LogParser<STEntry> {

  @Override
  public STEntry parse(String raw) throws ParserException {
    String[] entry = raw.split("\\s+");
    if(entry.length != 11) //Not a valid entry (e.g. newline)
      throw new ParserException("Invalid entry length");
    try {      
      double time = Float.valueOf(entry[0]);
      long utcTime = Long.valueOf(entry[1]);
      String callSign = entry[2];
      String acType = entry[3];
      String registration = entry[4];
      String status = entry[5];
      double x = Double.valueOf(entry[6]);
      double y = Double.valueOf(entry[7]);
      double z = Double.valueOf(entry[8]);
      double phi = Double.valueOf(entry[9]);
      double speed = Double.valueOf(entry[10]);
      return new STEntry(time, utcTime, callSign, acType, status, registration, x, y, z, phi, speed);
    } catch (NumberFormatException e) { // Not a valid entry
      throw new ParserException(e);
    }
  }
}
