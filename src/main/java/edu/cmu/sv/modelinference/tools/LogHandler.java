package edu.cmu.sv.modelinference.tools;

import java.io.InputStream;

import org.apache.commons.cli.ParseException;

import edu.cmu.sv.modelinference.tools.charting.LogProcessingException;

/**
 * @author Kasper Luckow
 *
 */
public interface LogHandler<T> {
  public String getHandlerName();
  public T process(String logFile, String logType, String[] additionalCmdArgs) throws LogProcessingException;
}
