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
package edu.cmu.sv.modelinference.mc.prism;

import edu.cmu.sv.modelinference.generators.model.Model;
import edu.cmu.sv.modelinference.generators.model.State;
import edu.cmu.sv.modelinference.generators.model.Transition;
import edu.cmu.sv.modelinference.generators.model.Assignment;
import edu.cmu.sv.modelinference.generators.model.WeightedTransition;
import edu.cmu.sv.modelinference.mc.ModelAdapter;
import edu.cmu.sv.modelinference.mc.ModelCheckerAdapter;
import edu.cmu.sv.modelinference.mc.ModelCheckerResult;
import edu.cmu.sv.modelinference.mc.PropertyAdapter;
import edu.cmu.sv.modelinference.mc.ModelCheckerResult.RES;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import parser.PrismParser;
import parser.ast.Command;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import parser.ast.Update;
import parser.ast.Updates;
import parser.type.Type;
import prism.ModelType;
import prism.Prism;
import prism.PrismCL;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import prism.Result;

/**
 * @author Kasper Luckow
 *
 */
public class PrismModelChecker extends ModelCheckerAdapter<ModulesFile, String> {
  private static final String MODULE_NAME = "prismmodule";

  private ModulesFile modulesFile;
  private Module module;
  
  @Override
  public void initModelGenerator(Model<?> model) {
    this.modulesFile = getModulesFileTemplate();
    this.module = getModuleTemplate(model, MODULE_NAME);
    this.modulesFile.addModule(this.module);
  }

  @Override
  public ModelCheckerResult executeModelChecker(ModelAdapter<ModulesFile> model, Collection<PropertyAdapter<String>> properties) {
    PrismFileLog mainLog = new PrismFileLog("stdout");
    PrismFileLog techLog = new PrismFileLog("stdout");

    Prism prism = new Prism(mainLog, techLog);
    try {
      prism.initialise();
    } catch (PrismException e) {
      throw new PrismModelGeneratorException(e);
    }
    try {
      this.modulesFile.tidyUp();
      prism.loadPRISMModel(this.modulesFile);
    } catch (PrismException e) {
      throw new PrismModelGeneratorException(e);
    }

    StringBuilder sb = new StringBuilder();
    Iterator<PropertyAdapter<String>> propIter = properties.iterator();
    while(propIter.hasNext()) {
      sb.append(propIter.next().getProperty());
      if(propIter.hasNext())
        sb.append("\n");
    }

    PropertiesFile propFile = null;
    try {
      propFile = prism.parsePropertiesString(this.modulesFile, sb.toString());
    } catch (PrismLangException e) {
      throw new PrismModelGeneratorException(e);
    }
    StringBuilder resultBuilder = new StringBuilder();
    for(int i = 0; i < propFile.getNumProperties(); i++) {
      try {
        Property checkedProp = propFile.getPropertyObject(i);
        Result res = prism.modelCheck(propFile, checkedProp);
        resultBuilder.append("Result for ").append(checkedProp.toString()).append(": ").append(res.getResultString());
        if(i < propFile.getNumProperties() - 1)
          resultBuilder.append("\n");
      } catch (PrismException e) {
        throw new PrismModelGeneratorException(e);
      }
    }
    //parse result correctly here to pass statistics and SAT/UNSAT

    return new ModelCheckerResult(RES.SAT, resultBuilder.toString(), "");
  }

  @Override
  public void finishModelGenerator() {
    //Nothing to do
  }

  public static ModulesFile getModulesFileTemplate() {
    ModulesFile modulesFile = new ModulesFile();
    modulesFile.setModelType(ModelType.DTMC);
    return modulesFile;
  }

  public static Module getModuleTemplate(Model<?> model, String moduleName) {
    final int defaultInitVal = 0;
    int initval = defaultInitVal;

    State initialState = model.getInitState();
    Module treeModule = new Module(moduleName);
    for(String modelVar : model.getModelVariables()) {
      int lowerBound = model.getMinAssignedVal(modelVar);
      int upperBound = model.getMaxAssignedVal(modelVar);

      if(upperBound == 0) {
        upperBound++;
        // logger.warn("Upper bound for variable [" + modelVar + "] is 0 and is therefore superfluous! Its value is incremented now to avoid PRISM errors.");
      } else if(lowerBound == upperBound) {
        //logger.warn("Upper and lower bound for variable [" + modelVar + "] is the same! The variable is superfluous.");
      }
      Declaration stateDecl = new Declaration(modelVar, new DeclarationInt(new ExpressionLiteral(parser.type.TypeInt.getInstance(), lowerBound), new ExpressionLiteral(parser.type.TypeInt.getInstance(), upperBound)));  
      Assignment<?> var = initialState.getAssignment(modelVar);
      if(var != null)
        initval = Integer.valueOf(var.getValue().toString());
      else
        initval = defaultInitVal;
      stateDecl.setStart(new ExpressionLiteral(parser.type.TypeInt.getInstance(), initval));
      treeModule.addDeclaration(stateDecl);
    }
    return treeModule;
  }

  private static Expression computeGuard(State state) {
    return recComputeGuard(null, state.getAssignments().iterator());
  }

  private static Expression recComputeGuard(Expression expr, Iterator<Assignment<?>> varIter) {
    if(varIter.hasNext()) {
      Assignment<?> var = varIter.next();
      ExpressionLiteral lit = computeExpressionLiteral(var);
      Expression eqEx = new ExpressionBinaryOp(ExpressionBinaryOp.EQ, computeExpressionIdent(var), lit);
      Expression nxt = null;
      if(expr != null)
        nxt = new ExpressionBinaryOp(ExpressionBinaryOp.AND, expr, eqEx);
      else
        nxt = eqEx;
      return recComputeGuard(nxt, varIter);
    }
    return expr;
  }

  private static ExpressionIdent computeExpressionIdent(Assignment<?> var) {
    return new ExpressionIdent(var.getName());
  }

  private static ExpressionLiteral computeExpressionLiteral(Assignment<?> var) {
    Object val = var.getValue();
    Type tp = null;
    if(val instanceof Double)
      tp = parser.type.TypeDouble.getInstance();
    else if(val instanceof Integer)
      tp = parser.type.TypeInt.getInstance();
    else if(val instanceof Boolean)
      tp = parser.type.TypeBool.getInstance();
    assert tp != null;
    return new ExpressionLiteral(tp, val.toString());
  }

  private static ExpressionLiteral computeExpressionLiteral(Assignment<?> var, String strVal) {
    Object val = var.getValue();
    Type tp = null;
    if(val instanceof Double)
      tp = parser.type.TypeDouble.getInstance();
    else if(val instanceof Integer)
      tp = parser.type.TypeInt.getInstance();
    else if(val instanceof Boolean)
      tp = parser.type.TypeBool.getInstance();
    assert tp != null;
    return new ExpressionLiteral(tp, strVal.toString());
  }

  private static Command getProbabilisticChoiceCmd(State currentState) {
    Command cmd = new Command();
    Expression guard = computeGuard(currentState);
    cmd.setGuard(guard);
    Updates updates = new Updates();
    if(currentState.getOutgoingTransitions().size() > 0) {
      for(Transition o : currentState.getOutgoingTransitions()) {
        Update update = new Update();
        State dest = o.getDest();
        for(Assignment<?> v : dest.getAssignments()) {
          update.addElement(computeExpressionIdent(v), computeExpressionLiteral(v));
        }
        if(o instanceof WeightedTransition) {
          WeightedTransition w = (WeightedTransition)o;
          updates.addUpdate(new ExpressionLiteral(parser.type.TypeDouble.getInstance(), w.getNormalizedWeight()), update);
        } else
          throw new IllegalStateException("Expected " + WeightedTransition.class.getName());
      }
    } else { //TODO: this should be fixed...
      Update update = new Update();
      for(Assignment<?> v : currentState.getAssignments()) { 
        update.addElement(computeExpressionIdent(v), computeExpressionLiteral(v, "-1"));
      }
      updates.addUpdate(new ExpressionLiteral(parser.type.TypeDouble.getInstance(), 1.0), update);
    }

    cmd.setUpdates(updates);
    return cmd;
  }

  @Override
  public <S extends State> void visit(S state) {
    Command cmd = getProbabilisticChoiceCmd(state);
    this.module.addCommand(cmd);
  }

  @Override
  public void visit(Transition transition) { }

  @Override
  public <S extends State> void visit(Model<S> model) { }

  @Override
  protected ModelAdapter<ModulesFile> getGeneratedModel() {
    return new PrismModel(this.modulesFile);
  }
}
