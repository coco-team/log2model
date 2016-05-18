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

import edu.cmu.sv.modelinference.common.generators.LogEntry;

/**
 * @author Kasper Luckow
 */
public class STEntry extends LogEntry {
  
  private final double phi, speed;
  private final long utcTime;
  private final String callSign, acType, status, registration;
  private final Coord3d position;

  public STEntry(double time, long utcTime, String callSign, String acType, String status, String registration, double x, double y, double z, double phi, double speed) {
    this(time, utcTime, callSign, acType, status, registration, new Coord3d(x, y, z), phi, speed);
  }
  
  public STEntry(double time, long utcTime, String callSign, String acType, String status, String registration, Coord3d position, double phi, double speed) { 
    super(time);
    this.utcTime = utcTime;
    this.callSign = callSign;
    this.acType = acType;
    this.status = status;
    this.registration = registration;
    this.position = position;
    this.phi = phi;
    this.speed = speed;
  }

  /**
   * @return the phi
   */
  public double getPhi() {
    return phi;
  }

  /**
   * @return the speed
   */
  public double getSpeed() {
    return speed;
  }

  /**
   * @return the utcTime
   */
  public long getUtcTime() {
    return utcTime;
  }

  /**
   * @return the callSign
   */
  public String getCallSign() {
    return callSign;
  }

  /**
   * @return the acType
   */
  public String getAcType() {
    return acType;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @return the registration
   */
  public String getRegistration() {
    return registration;
  }

  /**
   * @return the position
   */
  public Coord3d getPosition() {
    return position;
  }

  @Override
  public String getLogProducerId() {
    return this.getCallSign();
  }
}
