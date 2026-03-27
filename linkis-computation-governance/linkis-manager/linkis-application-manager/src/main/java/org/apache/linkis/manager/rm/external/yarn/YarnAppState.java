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

package org.apache.linkis.manager.rm.external.yarn;

/** Enumeration of Yarn application states. */
public enum YarnAppState {

  /** Application has been submitted but not yet accepted by ResourceManager. */
  NEW("NEW"),

  /** Application is being saved to persistent storage. */
  NEW_SAVING("NEW_SAVING"),

  /** Application has been submitted and is waiting for scheduling. */
  SUBMITTED("SUBMITTED"),

  /** Application has been accepted by ResourceManager and waiting for resource allocation. */
  ACCEPTED("ACCEPTED"),

  /** Application is running with at least one container executing. */
  RUNNING("RUNNING"),

  /** Application has completed successfully. */
  FINISHED("FINISHED"),

  /** Application execution has failed. */
  FAILED("FAILED"),

  /** Application has been manually terminated. */
  KILLED("KILLED");

  private final String state;

  YarnAppState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  /**
   * Check if the state is active (RUNNING or ACCEPTED). These states represent applications that
   * are consuming or about to consume cluster resources.
   *
   * @return true if the state is active
   */
  public boolean isActive() {
    return this == RUNNING || this == ACCEPTED;
  }

  /**
   * Parse string to YarnAppState enum.
   *
   * @param state the state string
   * @return YarnAppState enum
   */
  public static YarnAppState fromString(String state) {
    for (YarnAppState appState : YarnAppState.values()) {
      if (appState.state.equals(state)) {
        return appState;
      }
    }
    throw new IllegalArgumentException("Unknown YarnAppState: " + state);
  }
}
