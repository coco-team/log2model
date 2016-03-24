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

import java.util.Objects;

public class Vehicle implements Copyable<Vehicle> {

  private final String id;
  
  public Vehicle(String id) {
    this.id = id;
  }
  
  public String getId() {
    return this.id;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    
    Vehicle other = (Vehicle)obj;
    return Objects.equals(id, other.id);    
  }
  
  @Override
  public Vehicle copy() {
    return new Vehicle(this.id);
  }
  
  @Override
  public String toString() {
    return id;
  }
}
