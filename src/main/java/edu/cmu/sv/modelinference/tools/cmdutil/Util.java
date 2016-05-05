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
package edu.cmu.sv.modelinference.tools.cmdutil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.ParseException;

import edu.cmu.sv.modelinference.generators.formats.st.STModelInferer;

/**
 * @author Kasper Luckow
 *
 */
public class Util {
  public static class GridPartitions {
    public int vert;
    public int horiz;
    public static GridPartitions createDefault() {
      GridPartitions parts = new GridPartitions();
      parts.horiz = parts.vert = STModelInferer.DEF_PARTITIONS;
      return parts;
    }
  }

  public static GridPartitions extractGridPartitions(String optionString) throws ParseException {
    GridPartitions parts = new GridPartitions();
    parts.horiz = STModelInferer.DEF_PARTITIONS;
    parts.vert = STModelInferer.DEF_PARTITIONS;
    String optionStr = optionString.trim();
    String regex = "([0-9]+)x?([0-9]+)?";
    Matcher dimPatMatcher = Pattern.compile(regex).matcher(optionStr);
    if(dimPatMatcher.find()) {
      if(dimPatMatcher.groupCount() == 1) {
        parts.horiz = parts.vert = Integer.parseInt(dimPatMatcher.group(1));
      } else if(dimPatMatcher.groupCount() == 2) {
        String horizStr = dimPatMatcher.group(1);
        parts.horiz = Integer.parseInt(horizStr);
        String vertStr = dimPatMatcher.group(2);
        if(vertStr != null) //weird that this check is needed...
          parts.vert = Integer.parseInt(vertStr);
      } else {
        throw new ParseException("Dimensions must adhere to regex " + regex);
      }
    } else {
      throw new ParseException("Dimensions must adhere to regex " + regex);
    }
    return parts;
  }
}
