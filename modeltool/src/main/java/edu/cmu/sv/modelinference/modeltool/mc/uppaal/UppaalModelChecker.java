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
package edu.cmu.sv.modelinference.modeltool.mc.uppaal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sv.modelinference.modeltool.mc.ModelAdapter;
import edu.cmu.sv.modelinference.modeltool.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.modeltool.mc.ModelCheckerResult;
import edu.cmu.sv.modelinference.modeltool.mc.PropertyAdapter;
import edu.cmu.sv.modelinference.modeltool.model.Assignment;
import edu.cmu.sv.modelinference.modeltool.model.Model;
import edu.cmu.sv.modelinference.modeltool.model.State;
import edu.cmu.sv.modelinference.modeltool.model.WeightedTransition;
import uppaal.Automaton;
import uppaal.Location;
import uppaal.Location.LocationType;
import uppaal.NTA;
import uppaal.Transition;
import uppaal.labels.Probability;
import uppaal.labels.Update;

/**
 * @author Kasper Luckow
 *
 */
public class UppaalModelChecker extends ModelCheckerAdapter<NTA, String> {

	private static final String TMP_FOLDR = "/tmp/";
	private static final String UPPAAL_ENGINE_NAME = "verifyta";
	
	private static final int PROBABILITY_FACTOR = 100;
	private static final String PC_LOC_PREFIX = "PC";
	private static final String FINAL_LOC_PREFIX = "Final";
	private static final String NOND_LOC_PREFIX = "Nondet";
	
  private static Logger logger = LoggerFactory.getLogger(UppaalModelChecker.class.getName());
	
	private NTA nta;
	private Location initLoc;
	private Update initUpdate;
	private Automaton automaton;
	private Map<State, Location> translatedSources = new HashMap<>();
	 private Map<State, Location> translatedDestinations = new HashMap<>(); //fix this mess
	private String targetMethod = "target";
  private boolean isFirstLocSet = false;
	
	@Override
	public void initModelGenerator(Model<?> model) {
		this.nta = new NTA();
		this.automaton = new Automaton(targetMethod);
		this.nta.setSystemName(targetMethod + "_model");
		
    final int defaultInitVal = 0;
    int initVal;
		LinkedList<String> updates = new LinkedList<>();
		for(String modelVar : model.getModelVariables()) {
		  //TODO: Assume integer variables ONLY
      int lowerBound = model.getMinAssignedVal(modelVar);
      int upperBound = model.getMaxAssignedVal(modelVar);

      if(upperBound == 0) {
        upperBound++;
        logger.warn("Upper bound for variable [" + modelVar + "] is 0 and is therefore superfluous! Its value is incremented now to avoid PRISM errors.");
      } else if(lowerBound == upperBound) {
        //logger.warn("Upper and lower bound for variable [" + modelVar + "] is the same! The variable is superfluous.");
      }
      
      //TODO: get init val. Super ugly
      Assignment<?> var = model.getInitState().getAssignment(modelVar);
      if(var != null)
        initVal = Integer.valueOf(var.getValue().toString());
      else
        initVal = defaultInitVal;
      
      this.automaton.getDeclaration().add("int[" + lowerBound + "," + upperBound + "] " + modelVar + " = " + initVal + ";");
      updates.add(modelVar);
		}
		
		//this.initUpdate = new Update(updateStr);
		this.initLoc = new Location(this.automaton, "initloc");
		this.translatedSources.put(model.getInitState(), this.initLoc);
		this.initLoc.setType(LocationType.URGENT);
		this.automaton.setInit(this.initLoc);
		this.nta.addAutomaton(this.automaton);
		this.nta.getSystemDeclaration().addSystemInstance(automaton.getName().getName());
	}

	private String getOutput(Process process) throws IOException {
		String line;
		String stdoutString = "";
		String stderrString = "";

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();

		BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
		while ((line = brCleanUp.readLine()) != null) {
			stdoutString += "[Stdout] " + line + "\n";
		}
		brCleanUp.close();

		brCleanUp = new BufferedReader(new InputStreamReader(stderr));
		while ((line = brCleanUp.readLine()) != null) {
			stderrString += "[Stderr] " + line + "\n";
		}
		brCleanUp.close();

		return stdoutString + stderrString;
	}
	
	@Override
	public void finishModelGenerator(Model<?> irModel) {
	  for(State s : irModel.getStates()) {
	    Location srcLoc = this.translatedSources.get(s);
	    for(edu.cmu.sv.modelinference.modeltool.model.Transition t : s.getOutgoingTransitions()) {
	      if(!(t instanceof WeightedTransition)) {
	        throw new UppaalModelGeneratorException("Expected weighted transition");
	      }
        WeightedTransition wt = (WeightedTransition)t;
	      State nextState = t.getDest();
	      Location destLoc = this.translatedDestinations.get(nextState);

	      Transition trans = new Transition(this.automaton, srcLoc, destLoc);
	      trans.setProb(new Probability(wt.getWeight()));
	    }
	  }
	}

  @Override
  public <S extends State> void visit(S state) {
    Location branchLoc = new Location(this.automaton);
    branchLoc.setBranchPointLocation(true);
    this.translatedSources.put(state, branchLoc);
    Location translatedLoc = new Location(this.automaton, state.toString().replaceAll("[\\s,=]*", ""));
    translatedLoc.setType(LocationType.URGENT);
    new Transition(this.automaton, translatedLoc, branchLoc);
    if(!isFirstLocSet) {
      new Transition(this.automaton, this.initLoc, translatedLoc);
      isFirstLocSet = true;
    }
    this.translatedDestinations.put(state, translatedLoc);
  }
  
  @Override
  public void visit(edu.cmu.sv.modelinference.modeltool.model.Transition transition) {
    // TODO Auto-generated method stub
    
  }
  @Override
  public <S extends State> void visit(Model<S> model) {
    // TODO Auto-generated method stub
  }
  
  @Override
  protected ModelAdapter<NTA> getGeneratedModel() {
    return new UppaalModel(this.nta);
  }
  
  @Override
  public ModelCheckerResult executeModelChecker(ModelAdapter<NTA> model,
      Collection<PropertyAdapter<String>> properties) throws UppaalModelCheckerException {
    String OS = System.getProperty("os.name").toLowerCase();
    boolean isUnix = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;
    boolean isMac = OS.indexOf("mac") >= 0;
    //TODO: Fix this restriction...
    if(!isUnix && !isMac)
      throw new UppaalModelCheckerException("Currently, uppaal can only be run on Mac or Linux/Unix");
    if(!Files.exists(Paths.get(TMP_FOLDR)))
      throw new UppaalModelCheckerException("To run the verification engine of uppaal directly, you must have write access to " + TMP_FOLDR);
    
    String modelPath = "";
    String queryPath = "";
    //TODO: we should reuse the model files written to the output path if that is specified in the config. This would 
    //prevent writing the files twice.
    try {
      model.writeModelToFile(TMP_FOLDR);
    } catch (IOException e1) {
      throw new UppaalModelCheckerException(e1);
    }

    Process process;
    try {
      process = Runtime.getRuntime().exec(UPPAAL_ENGINE_NAME + " " + modelPath + " " + queryPath);
      String output = getOutput(process);
    } catch (IOException e) {
      throw new UppaalModelCheckerException(e);
    }    

    //FIX this
    return null;
  }
}
