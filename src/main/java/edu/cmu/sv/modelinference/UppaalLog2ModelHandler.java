package edu.cmu.sv.modelinference;

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
