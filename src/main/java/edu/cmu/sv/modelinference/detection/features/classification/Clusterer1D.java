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
package edu.cmu.sv.modelinference.detection.features.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.TreeMultiset;

/**
 * @author Kasper Luckow
 *
 */
public class Clusterer1D implements EventClassifier {
  private class DataWrapper implements Clusterable {
    private final Event event;

    public DataWrapper(Event event) {
      this.event = event;
    }    

    public Event getWrappedData() {
      return this.event;
    }

    @Override
    public double[] getPoint() {
      return new double[] {0, event.getFeature().getData()};
    }
    
    @Override
    public String toString() {
      return "(0, " + event.getFeature().getData() + ")";
    }
  }
  
  private static final Logger logger = LoggerFactory.getLogger(Clusterer1D.class);

  private final static int MAX_K_DEFAULT = 5;
  private final static int MAX_ITERATIONS_DEFAULT = -1;
  private final static int TRIALS_DEFAULT = 5;

  private final int maxK;
  private final int maxIterations;
  private final int trials;

  public Clusterer1D(int maxK, int maxIterations, int trials) {
    this.maxIterations = maxIterations;
    this.maxK = maxK;
    this.trials = trials;
  }
  
  public Clusterer1D(int maxIterations) {
    this(MAX_K_DEFAULT, maxIterations, TRIALS_DEFAULT);
  }

  public Clusterer1D() {
    this(MAX_K_DEFAULT, MAX_ITERATIONS_DEFAULT, TRIALS_DEFAULT);
  }  

  private static ClassificationResult buildResult(List<? extends Cluster<DataWrapper>> results) {
    LinkedList<EventClass> clusters = new LinkedList<>();
    for(int i = 0; i < results.size(); i++) {
      TreeMultiset<Event> clusterDataPoints = TreeMultiset.create(new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
          return Double.compare(o1.getFeature().getData(), o2.getFeature().getData());
        }
      });
      for(DataWrapper dataWrapper : results.get(i).getPoints()) {
        clusterDataPoints.add(dataWrapper.getWrappedData());          
      }
      clusters.addLast(new EventClass(clusterDataPoints));
    }
    return new ClassificationResult(clusters);
  }

  @Override
  public ClassificationResult classify(Collection<Event> events) {
    Collection<DataWrapper> dataCol = new ArrayList<>();
    for(Event event : events) {
      dataCol.add(new DataWrapper(event));
    }
    List<? extends Cluster<DataWrapper>> clusterResults = null;
    int currK = this.maxK;
    while((clusterResults = computeClusters(dataCol, currK)) == null) {
      logger.warn("Lowering k from " + currK + " to " + (currK - 1));
      currK--;
    }
    logger.info("Constructed cluster with " + currK + " classes");
    return buildResult(clusterResults);
  }
  
  private List<? extends Cluster<DataWrapper>> computeClusters(Collection<DataWrapper> dataCol, int k) {
    List<? extends Cluster<DataWrapper>> clusterResults = null;
    try {
    Clusterer<DataWrapper> clusterer = new MultiKMeansPlusPlusClusterer<>(new KMeansPlusPlusClusterer<DataWrapper>(k, maxIterations), trials);
    clusterResults = clusterer.cluster(dataCol);
    } catch(NumberIsTooSmallException e) {
      logger.warn("Too few datapoints for clusters: " + e.getMessage());
    }
    return clusterResults;
  }
}
