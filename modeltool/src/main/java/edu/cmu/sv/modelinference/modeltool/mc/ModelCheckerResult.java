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
package edu.cmu.sv.modelinference.modeltool.mc;


/**
 * @author Kasper Luckow
 *
 */
public class ModelCheckerResult {
  
  public enum RES {
    SAT,
    UNSAT,
    DONT_KNOW
  }
  
  private final String analysisResult;
  private final RES result;
  private final String statistics;
  
  public ModelCheckerResult(RES result, String analysisResult, String statistics) {
    this.result = result;
    this.analysisResult = analysisResult;
    this.statistics = statistics;
  }
  
  
  public String getAnalysisResultString() {
    return this.analysisResult;
  }
  
  public RES getResult() {
    return result;
  }
  
  public String getStatistics() {
    return this.statistics;
  }
}
