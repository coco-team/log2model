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
package edu.cmu.sv.modelinference.generators.parser.st;

/**
 * @author Kasper Luckow
 */
public class Coord2d {
  private final double x, y;
  
  public static Coord2d project(Coord3d coord3d) {
    return new Coord2d(coord3d.getX(), coord3d.getY());
  }
  
  public Coord2d(double x, double y) {
    this.x = x; this.y = y;
  }
  public double getX() {
    return this.x;
  }
  
  public double getY() {
    return this.y;
  }
}
