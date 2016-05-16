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
package edu.cmu.sv.modelinference.common.formats.autoresolver;

import edu.cmu.sv.modelinference.generators.LogEntry;

/**
 * @author Kasper Luckow
 */
public class AutoresolverEntry extends LogEntry {

  private final String flightName;
  private final double altitude, altitudeRateFpm, 
                       fuelWeight, groundSpeed, 
                       heading, indicatedSpeed, 
                       latDegrees, lonDegrees, 
                       trueAirspeed, trueCourse; 
  
  public AutoresolverEntry(double logTime, String flightName, 
                           double altitude, double altitudeRateFpm, 
                           double fuelWeight, double groundSpeed, 
                           double heading, double indicatedSpeed, 
                           double latDegrees, double lonDegrees, 
                           double trueAirspeed, double trueCourse) {
    super(logTime);
    this.flightName = flightName;
    this.altitude = altitude;
    this.altitudeRateFpm = altitudeRateFpm;
    this.fuelWeight = fuelWeight;
    this.groundSpeed = groundSpeed;
    this.heading = heading;
    this.indicatedSpeed = indicatedSpeed;
    this.latDegrees = latDegrees;
    this.lonDegrees = lonDegrees;
    this.trueAirspeed = trueAirspeed;
    this.trueCourse = trueCourse;
  }

  /**
   * @return the flightName
   */
  public String getFlightName() {
    return flightName;
  }

  /**
   * @return the altitude
   */
  public double getAltitude() {
    return altitude;
  }

  /**
   * @return the altitudeRateFpm
   */
  public double getAltitudeRateFpm() {
    return altitudeRateFpm;
  }

  /**
   * @return the fuelWeight
   */
  public double getFuelWeight() {
    return fuelWeight;
  }

  /**
   * @return the groundSpeed
   */
  public double getGroundSpeed() {
    return groundSpeed;
  }

  /**
   * @return the heading
   */
  public double getHeading() {
    return heading;
  }

  /**
   * @return the indicatedSpeed
   */
  public double getIndicatedSpeed() {
    return indicatedSpeed;
  }

  /**
   * @return the latDegrees
   */
  public double getLatDegrees() {
    return latDegrees;
  }

  /**
   * @return the lonDegrees
   */
  public double getLonDegrees() {
    return lonDegrees;
  }

  /**
   * @return the trueAirspeed
   */
  public double getTrueAirspeed() {
    return trueAirspeed;
  }

  /**
   * @return the trueCourse
   */
  public double getTrueCourse() {
    return trueCourse;
  }

  @Override
  public String getLogProducerId() {
    return this.flightName;
  }
}
