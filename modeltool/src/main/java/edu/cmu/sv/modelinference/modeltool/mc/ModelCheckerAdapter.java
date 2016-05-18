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

import java.util.Collection;

import edu.cmu.sv.modelinference.common.model.Model;
import edu.cmu.sv.modelinference.common.model.ModelVisitor;
import edu.cmu.sv.modelinference.common.model.State;

/**
 * @author Kasper Luckow
 * T, type for the model produced. S, the type of the properties
 */
public abstract class ModelCheckerAdapter<T, S> implements ModelVisitor {
	
	public ModelAdapter<T> generateModel(Model<?> irModel) {
	  for(State s : irModel.getStates())
	    s.setVisited(false);
	  initModelGenerator(irModel);
	  irModel.accept(this);
	  finishModelGenerator(irModel);
	  return getGeneratedModel();
	}
	
  public ModelCheckerResult executeModelChecker(Model<?> irModel, Collection<PropertyAdapter<S>> properties) throws ModelCheckerException {
    ModelAdapter<T> model = generateModel(irModel);
    return executeModelChecker(model, properties);
  }
  
  public abstract ModelCheckerResult executeModelChecker(ModelAdapter<T> model, Collection<PropertyAdapter<S>> properties) throws ModelCheckerException;

	protected abstract void initModelGenerator(Model<?> model);
	protected abstract void finishModelGenerator(Model<?> model);
	protected abstract ModelAdapter<T> getGeneratedModel();
}
