package edu.cmu.sv.modelinference;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.cmu.sv.modelinference.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.mc.prism.PrismModelChecker;
import edu.cmu.sv.modelinference.tools.LogHandler;
import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;

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
