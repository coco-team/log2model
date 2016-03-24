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
package edu.cmu.sv.modelinference.generators.parser.st;

import java.io.IOException;

import edu.cmu.sv.modelinference.generators.IRModelGenerator;
import edu.cmu.sv.modelinference.generators.LogEntryFilter;
import edu.cmu.sv.modelinference.generators.ModelInferer;
import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.generators.parser.reader.SequentialLogReader;
import edu.cmu.sv.modelinference.generators.parser.st.util.GridDimensionsFinder;
import edu.cmu.sv.modelinference.generators.parser.st.util.GridDimensionsFinder.Dimensions;

public class STModelInferer implements ModelInferer<GridState> {
  
  public static final int DEF_PARTITIONS = 2;
  
  private final int xPart;
  private final int yPart;
  
  public STModelInferer(int xPart, int yPart) {
    this.xPart = xPart;
    this.yPart = yPart;
  }
  
  @Override
  public Model<GridState> generateModel(String logFile) throws IOException {
    GridDimensionsFinder dimFinder = new GridDimensionsFinder(new LogEntryFilter<STEntry>() {
      @Override
      public boolean submitForProcessing(STEntry entry) {
        return true;
      }
    });
    Dimensions dim = dimFinder.start(logFile);
    
    IRModelGenerator<STEntry, GridState> irGen = new IRModelGenerator<>(
        new SequentialLogReader<>(
            new STParser()), 
        new STGridStateFactory(
            new Coord2d(dim.minX, dim.minY), 
            new Coord2d(dim.maxX, dim.maxY),
            xPart,
            yPart));

    return irGen.generateModel(logFile);
  }
}
