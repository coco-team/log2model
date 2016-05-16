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
package edu.cmu.sv.modelinference.modeltool.pp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import att.grappa.Attribute;
import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.Node;
import edu.cmu.sv.modelinference.modeltool.model.Model;
import edu.cmu.sv.modelinference.modeltool.model.ModelVisitor;
import edu.cmu.sv.modelinference.modeltool.model.State;
import edu.cmu.sv.modelinference.modeltool.model.Transition;
import edu.cmu.sv.modelinference.modeltool.model.WeightedTransition;

/**
 * @author Kasper Luckow
 *
 */
public class PrettyPrinter implements ModelVisitor {
		
	private int uniqueID;
	private Map<State, Node> visitedStates = new HashMap<>();
	private Graph grappaGraph;
	private Model<? extends State> model;
	
	public PrettyPrinter(Model<? extends State> model) {
		this.uniqueID = 0;
		this.model = model;
		this.grappaGraph = new Graph("Model");
	}
	
	public void printModel(String outputPath, Format format) {
	   for(State s : this.model.getStates())
	      s.setVisited(false);
		this.model.accept(this);
		
		//TODO: This is super ugly....
		this.addEdges(model);
		
    try {
      File file = File.createTempFile("prettyprint", ".dot", new File(outputPath));
			FileOutputStream fo = new FileOutputStream(file);
			grappaGraph.printGraph(fo);
			fo.close();
			convertDotFile(file, format);
			file.delete();
		} catch (IOException | InterruptedException e) {
			throw new PrettyPrintException(e);
		}
	}
	
	private void addEdges(Model<? extends State> model) {
	  for(State s : model.getStates()) {
	    for(Transition t : s.getOutgoingTransitions()) {
	      if(t.getDest() != null) {
	        Node srcNode = this.visitedStates.get(s);
	        Node destNode = this.visitedStates.get(t.getDest());
	        Edge edge = new Edge(this.grappaGraph, srcNode, destNode);
	        if(t instanceof WeightedTransition) {
	          String label = String.format(((WeightedTransition)t).getWeight() + 
            "\\n(%.3f)", ((WeightedTransition)t).getNormalizedWeight());
	          edge.setAttribute(Attribute.LABEL_ATTR, label);
	        }
	        this.grappaGraph.addEdge(edge);
	      }
	    }
	  }
	}

	@Override
	public void visit(State state) {
		this.constructNode(state, getAttrs(state, Attribute.OVAL_SHAPE));
	}
	
	private void constructNode(State state, List<Attribute> attrs) {
	  if(this.visitedStates.containsKey(state))
	    return;
		Node targetNode = new Node(grappaGraph, Integer.toString(this.uniqueID++));
		for(Attribute attr : attrs)
			targetNode.setAttribute(attr);
		this.visitedStates.put(state, targetNode);
	}
	
	 private List<Attribute> getAttrs(State state, int shape) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(state.toString().replaceAll("\\n", "\\\\n"));
	    List<Attribute> attrs = new LinkedList<>();
	    attrs.add(new Attribute(Attribute.NODE, Attribute.SHAPE_ATTR, shape));
	    attrs.add(new Attribute(Attribute.NODE, Attribute.LABEL_ATTR, sb.toString()));
	    if(state.equals(model.getInitState())) {
	      attrs.add(new Attribute(Attribute.NODE, Attribute.STYLE_ATTR, "filled"));
	      attrs.add(new Attribute(Attribute.NODE, Attribute.FILLCOLOR_ATTR, "gray"));
	    }
	    return attrs;
	  }
	
	private void convertDotFile(File file, Format format) throws InterruptedException {
		String dotCmd = "/usr/local/bin/dot " + file.getPath() + " -T" + format.getFormat() + " -o " + file.getPath().replace(".dot", "." + format.getFormat());
		try {
			Process p = Runtime.getRuntime().exec(dotCmd);
			p.waitFor();
			p.exitValue();
			p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

  @Override
  public void visit(Transition transition) {  }

  @Override
  public <S extends State> void visit(Model<S> model) {
    // TODO Auto-generated method stub
    
  }
}
