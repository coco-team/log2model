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

import java.util.Iterator;

import edu.cmu.sv.modelinference.common.formats.st.GridFactory.Grid.Cell;
import edu.cmu.sv.modelinference.common.model.Assignment;
import edu.cmu.sv.modelinference.common.model.State;

/**
 * @author Kasper Luckow
 */
public class GridState extends State {

  private final GridFactory.Grid<Vehicle> grid;

  public GridState(GridFactory.Grid<Vehicle> grid) {
    this.grid = grid;
    for(Cell<Vehicle> cell : grid.getCells()) {
      Assignment<Integer> assign = new Assignment<Integer>("q_" + cell.getX() + "_" + cell.getY(),
          (cell.getData() == null) ?
              0 :
              cell.getData().size());
      this.addAssignment(assign);
    }
  }
  
  public GridFactory.Grid<Vehicle> getGrid() {
    return this.grid;
  }
  
  public String toSimpleString() {
    StringBuilder sb = new StringBuilder();
    Iterator<Assignment<?>> assignmentIter = this.assignments.iterator();
    while(assignmentIter.hasNext()) {
      sb.append(assignmentIter.next().toString());
      if(assignmentIter.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return toSimpleString();//this.grid.toString();
  }
}
