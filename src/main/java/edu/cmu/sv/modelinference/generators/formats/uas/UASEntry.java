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
package edu.cmu.sv.modelinference.generators.formats.uas;

import edu.cmu.sv.modelinference.generators.LogEntry;

/**
 * @author Kasper Luckow
 * UAS has numerous other fields --- this log entry represents a subset of them
 */
public class UASEntry extends LogEntry {

  //time is in [ms]
  private final double lat, //[rad] 
                      lon, //[rad]
                      height, //[m] 
                      speed, //groundspeed [m/s]
                      direction, //[rad]
                      roll, //[rad]
                      pitch, //[rad]
                      yaw, //[rad]
                      alt; //[m]
  
  public UASEntry(double logTime, double lat, double lon, double alt, double height, double speed, double direction, double roll, double pitch, double yaw) {
    super(logTime);
    this.lat = lat;
    this.lon = lon;
    this.height = height;
    this.speed = speed;
    this.direction = direction;
    this.roll = roll;
    this.pitch = pitch;
    this.yaw = yaw;
    this.alt = alt;
  }

  /**
   * @return the lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * @return the lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * @return the height
   */
  public double getHeight() {
    return height;
  }

  /**
   * @return the speed
   */
  public double getSpeed() {
    return speed;
  }

  /**
   * @return the direction
   */
  public double getDirection() {
    return direction;
  }

  /**
   * @return the roll
   */
  public double getRoll() {
    return roll;
  }

  /**
   * @return the pitch
   */
  public double getPitch() {
    return pitch;
  }

  /**
   * @return the yaw
   */
  public double getYaw() {
    return yaw;
  }

  /**
   * @return the alt
   */
  public double getAlt() {
    return alt;
  }

  @Override
  public String getLogProducerId() {
    //set id
    return "";
  }
}
