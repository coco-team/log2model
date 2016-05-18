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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.cmu.sv.modelinference.common.api.LogHandler;
import edu.cmu.sv.modelinference.common.api.LogProcessingException;
import edu.cmu.sv.modelinference.modeltool.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.modeltool.mc.prism.PrismModelChecker;

/**
 * @author Kasper Luckow
 *
 */
public class PrismLog2ModelHandler implements LogHandler<ModelCheckerAdapter<?, ?>> {

  private static PrismLog2ModelHandler instance = null;
  
  public static PrismLog2ModelHandler getInstance() {
    if(instance == null) {
      instance = new PrismLog2ModelHandler();
    }
    return instance;
  }
  
  private PrismLog2ModelHandler() {  }

  @Override
  public String getHandlerName() {
    return "prism";
  }

  @Override
  public ModelCheckerAdapter<?, ?> process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException {
    return new PrismModelChecker();
  }
}
