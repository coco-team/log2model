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
package edu.cmu.sv.modelinference.generators;

import edu.cmu.sv.modelinference.generators.formats.st.GridState;
import edu.cmu.sv.modelinference.generators.model.State;

/**
 * @author Kasper Luckow
 */
public interface StateFactory<T extends LogEntry, S extends State> {
  public S generateState(S currState, T entry);
  public void addEntryToState(S currState, T entry);
  public boolean isNewState(S currState, T entry);
  
  //These will be removed eventually
  @Deprecated
  public S finalizeState(S currState);
  @Deprecated
  public S generateState();
}
