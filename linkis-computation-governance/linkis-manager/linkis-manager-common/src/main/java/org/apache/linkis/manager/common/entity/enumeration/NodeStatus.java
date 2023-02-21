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

package org.apache.linkis.manager.common.entity.enumeration;

import org.apache.commons.lang3.StringUtils;

public enum NodeStatus {

  /**
   * Starting, EC start state
   *
   * <p>Unlock, EC idle state
   *
   * <p>Locked state: This state contains two substates, Idle and Busy
   *
   * <p>Idle, which locks the no-task state
   *
   * <p>Busy, task execution status
   *
   * <p>Running,ECM exclusive state,EC will not have this state
   *
   * <p>ShuttingDown,EC abnormal exit state
   *
   * <p>Failed, Once EC executes the exception failure state and the start failure state
   *
   * <p>Success; The EC exits the state normally
   */
  Starting,
  Unlock,
  Locked,
  Idle,
  Busy,
  Running,
  ShuttingDown,
  Failed,
  Success;

  public static Boolean isAvailable(NodeStatus status) {
    if (Idle == status
        || Busy == status
        || Locked == status
        || Unlock == status
        || Running == status) {
      return true;
    }
    return false;
  }

  public static Boolean isLocked(NodeStatus status) {
    if (Busy == status || Locked == status || Idle == status) {
      return true;
    }
    return false;
  }

  public static Boolean isIdle(NodeStatus status) {
    if (Idle == status || Unlock == status) {
      return true;
    }
    return false;
  }

  public static Boolean isCompleted(NodeStatus status) {
    if (Success == status || Failed == status || ShuttingDown == status) {
      return true;
    }
    return false;
  }

  public static NodeStatus toNodeStatus(String status) throws IllegalArgumentException {
    if (StringUtils.isBlank(status)) {
      throw new IllegalArgumentException(
          "Invalid status : " + status + " cannot be matched in NodeStatus");
    }
    return NodeStatus.valueOf(status);
  }

  public static NodeHealthy isEngineNodeHealthy(NodeStatus status) {
    switch (status) {
      case Starting:
      case Running:
      case Busy:
      case Idle:
      case Locked:
      case Unlock:
      case Success:
        return NodeHealthy.Healthy;
      case Failed:
      case ShuttingDown:
        return NodeHealthy.UnHealthy;
      default:
        return NodeHealthy.UnHealthy;
    }
  }
}
