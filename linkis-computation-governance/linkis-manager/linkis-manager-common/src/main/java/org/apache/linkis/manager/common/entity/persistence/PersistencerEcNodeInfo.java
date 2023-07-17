/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.common.entity.persistence;

/** engine instance info entity */
public class PersistencerEcNodeInfo extends PersistenceNode {

  private Integer instanceStatus;

  private String heartbeatMsg;

  private String engineType;

  public Integer getInstanceStatus() {
    return instanceStatus;
  }

  public void setInstanceStatus(Integer instanceStatus) {
    this.instanceStatus = instanceStatus;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getHeartbeatMsg() {
    return heartbeatMsg;
  }

  public void setHeartbeatMsg(String heartbeatMsg) {
    this.heartbeatMsg = heartbeatMsg;
  }

  @Override
  public String toString() {
    return "PersistencerEcNodeInfo{"
        + "instanceStatus="
        + instanceStatus
        + ", engineType='"
        + engineType
        + '\''
        + "} "
        + super.toString();
  }
}
