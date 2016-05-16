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
package edu.cmu.sv.modelinference.common.formats.st;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Kasper Luckow
 */
public class GridFactory<S extends Copyable<S>> {

  public static class Grid<T extends Copyable<T>> implements Copyable<Grid<T>> {
    
    public static class Cell<T> {
      private final int x, y;
      private final Collection<T> data;
      
      public Cell(int x, int y, Collection<T> data) {
        this.x = x;
        this.y = y;
        this.data = data;
      }

      public int getX() {
        return x;
      }

      public int getY() {
        return y;
      }
      
      public Collection<T> getData() {
        return data;
      }
    }
    
    private final NavigableMap<Double, NavigableMap<Double, Collection<T>>> xTree;
    private final Map<T, Collection<T>> obj2coll;
    
    private Grid(NavigableMap<Double, NavigableMap<Double, Collection<T>>> xTree) {
      this(xTree, new HashMap<>());
    }

    private Grid(NavigableMap<Double, NavigableMap<Double, Collection<T>>> xTree, Map<T, Collection<T>> obj2coll) {
      this.xTree = xTree;
      this.obj2coll = obj2coll;
    }
    
    public Collection<Cell<T>> getCells() {
      Set<Cell<T>> cells = new HashSet<>(); 
      int xCount = 0;
      for(Entry<Double, NavigableMap<Double, Collection<T>>> x : xTree.entrySet()) {
        int yCount = 0;
        for(Entry<Double, Collection<T>> y : x.getValue().entrySet()) {
          cells.add(new Cell<>(xCount, yCount, y.getValue()));
          yCount++;
        }
        xCount++;
      }
      return cells;
    }
    
    public Collection<T> getCell(Coord2d coord) {
      return getCellEntry(coord).getValue();
    }

    private Entry<Double, Collection<T>> getCellEntry(Coord2d coord) {
      NavigableMap<Double, Collection<T>> ytree = getYtree(coord);
      return ytree.floorEntry(coord.getY());
    }
    
    private NavigableMap<Double, Collection<T>> getYtree(Coord2d coord) {
      return this.xTree.floorEntry(coord.getX()).getValue();
    }
    
    public void remove(T obj) {
      if(obj2coll.containsKey(obj)) {
        obj2coll.get(obj).remove(obj);
        obj2coll.remove(obj);
      }
    }
    
    public void add(T obj, Coord2d coord) {
      if(obj2coll.containsKey(obj)) { // establish invariant, that obj is ONLY in one cell
        obj2coll.get(obj).remove(obj);
        obj2coll.remove(obj);
      } 
      NavigableMap<Double, Collection<T>> ytree = getYtree(coord);
      Collection<T> cell = ytree.floorEntry(coord.getY()).getValue();
      cell.add(obj);
      obj2coll.put(obj, cell);
    }
    
    public boolean contains(T obj) {
      return obj2coll.containsKey(obj);
    }
    
    public boolean contains(T obj, Coord2d coord) {
      Collection<T> cell = getCell(coord);
      if(cell == null)
        return false;
      return cell.contains(obj);
    }
    
    @Override  
    public int hashCode() {
      return com.google.common.base.Objects.hashCode(xTree);  
    }
    
    @Override  
    public boolean equals(Object obj) {
      if(obj == null) {
        return false;
      }
      if(getClass() != obj.getClass()) {  
        return false;
      }
      
      final Grid<?> other = (Grid<?>) obj;
      return com.google.common.base.Objects.equal(this.xTree, other.xTree);
    }
    
    @Override
    public Grid<T> copy() { // super, super, super expensive
      Map<T, Collection<T>> o2colcpy = new HashMap<>();
      NavigableMap<Double, NavigableMap<Double, Collection<T>>> cpyXTree = new TreeMap<>();
      for(double x : xTree.keySet()) {
        NavigableMap<Double, Collection<T>> cpyYTree = new TreeMap<>();
        cpyXTree.put(x, cpyYTree);
        NavigableMap<Double, Collection<T>> yEntry = xTree.get(x);
        for(double y : yEntry.keySet()) {
          Collection<T> oldColl = yEntry.get(y);
          Set<T> newSet = new HashSet<>();
          for(T obj : oldColl) {
            T newObj = obj.copy();
            newSet.add(newObj);
            o2colcpy.put(newObj, newSet);
          }
          cpyYTree.put(y, newSet);
        }
      }
      return new Grid<>(cpyXTree,o2colcpy);
    }
    
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      
      int xCount = 0;
      for(Entry<Double, NavigableMap<Double, Collection<T>>> x : xTree.entrySet()) {
        int yCount = 0;
        for(Entry<Double, Collection<T>> y : x.getValue().entrySet()) {
          if(!y.getValue().isEmpty()) {
            sb.append('[').append(xCount).append(',').append(yCount).append("]:{");
            Iterator<T> iter = y.getValue().iterator();
            while(iter.hasNext()) {
              sb.append(iter.next().toString());
              if(iter.hasNext())
                sb.append(",");
            }
            sb.append("}; ");
          }
          yCount++;
        }
        xCount++;
      }
      String s = sb.toString();
      return (s.length() > 2) ? s.substring(0, s.length()-2) : s; //ugly
    }
  }
  
  private final double xfac, yfac;
  private final Coord2d lowerLeft, upperRight;
  private final int horizPartitions, vertPartitions;
  
  public GridFactory(Coord2d lowerLeft, Coord2d upperRight, int horizPartitions, int vertPartitions) {
    xfac = Math.abs((upperRight.getX() - lowerLeft.getX())) / (double)horizPartitions;
    yfac = Math.abs((upperRight.getY() - lowerLeft.getY())) / (double)vertPartitions;
    this.lowerLeft = lowerLeft;
    this.upperRight = upperRight;
    this.horizPartitions = horizPartitions;
    this.vertPartitions = vertPartitions;
  }
  
  public Grid<S> build() {
    NavigableMap<Double, NavigableMap<Double, Collection<S>>> xTree = new TreeMap<>();    
    int xPart = 0;
    for(double xAcc = lowerLeft.getX(); xPart < horizPartitions; xAcc += xfac) {
      NavigableMap<Double, Collection<S>> yTree = new TreeMap<>();
      int yPart = 0;
      for(double yAcc = lowerLeft.getY(); yPart < vertPartitions; yAcc += yfac) {
        yTree.put(yAcc, new HashSet<>());
        yPart++;
      }
      xTree.put(xAcc, yTree);
      xPart++;
    }
    return new Grid<>(xTree);
  }  
}
