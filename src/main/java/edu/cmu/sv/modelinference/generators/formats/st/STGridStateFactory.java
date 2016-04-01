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
package edu.cmu.sv.modelinference.generators.formats.st;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.cmu.sv.modelinference.generators.formats.st.GridFactory.Grid;

/**
 * @author Kasper Luckow
 */
public class STGridStateFactory implements STStateFactory<GridState> {
  private final GridFactory<Vehicle> gridFactory;
  
  private LoadingCache<String, Vehicle> vehicleCache = CacheBuilder
      .newBuilder()
      .maximumSize(1000)
      .build(
          new CacheLoader<String, Vehicle>() {
            @Override
            public Vehicle load(String key) throws Exception {
              return new Vehicle(key);
            }
          });
  
  public STGridStateFactory(Coord2d lowerLeft, Coord2d upperRight, int horizPartitions, int vertPartitions) {
    this.gridFactory = new GridFactory<>(lowerLeft, upperRight, horizPartitions, vertPartitions);
  }

  @Override
  public GridState generateState(GridState currState, STEntry entry) {
    Grid<Vehicle> newGrid = null;
    if(currState == null) {
      newGrid = gridFactory.build();
    } else {
      newGrid = currState.getGrid().copy();
    }
    
    newGrid.add(vehicleCache.getUnchecked(entry.getCallSign()), Coord2d.project(entry.getPosition()));
    
    return new GridState(newGrid);
  }

  @Override
  public boolean isNewState(GridState currState, STEntry entry) {
    Grid<Vehicle> currGrid = currState.getGrid();
    return !currGrid.contains(
        vehicleCache.getUnchecked(entry.getCallSign()),
        Coord2d.project(entry.getPosition()));
  }

  @Override
  public void addEntryToState(GridState currState, STEntry entry) {
    Vehicle v = vehicleCache.getUnchecked(entry.getCallSign());
    currState.getGrid().add(v, Coord2d.project(entry.getPosition()));
  }
}
