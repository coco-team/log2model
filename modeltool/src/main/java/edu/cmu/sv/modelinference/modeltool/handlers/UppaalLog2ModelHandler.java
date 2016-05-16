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
package edu.cmu.sv.modelinference.modeltool.handlers;

import edu.cmu.sv.modelinference.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.mc.uppaal.UppaalModelChecker;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;

/**
 * @author Kasper Luckow
 *
 */
public class UppaalLog2ModelHandler implements LogHandler<ModelCheckerAdapter<?, ?>> {

  private static UppaalLog2ModelHandler instance = null;
  
  public static UppaalLog2ModelHandler getInstance() {
    if(instance == null) {
      instance = new UppaalLog2ModelHandler();
    }
    return instance;
  }
  
  private UppaalLog2ModelHandler() {  }
  
  
  @Override
  public String getHandlerName() {
    return "uppaal";
  }

  
  @Override
  public ModelCheckerAdapter<?, ?> process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    return new UppaalModelChecker();
  }

}
