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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.status;

import java.util.ArrayList;
import java.util.List;

/** Stores savepoint related information. */
public class SavepointInfo {
  /** Last completed savepoint by the operator. */
  private Savepoint lastSavepoint;

  /** Trigger id of a pending savepoint operation. */
  private String triggerId;

  /** Trigger timestamp of a pending savepoint operation. */
  private Long triggerTimestamp;

  /** Savepoint trigger mechanism. */
  private SavepointTriggerType triggerType;

  /** Savepoint format. */
  private SavepointFormatType formatType;

  /** List of recent savepoints. */
  private List<Savepoint> savepointHistory = new ArrayList<>();

  /** Trigger timestamp of last periodic savepoint operation. */
  private long lastPeriodicSavepointTimestamp = 0L;

  public Savepoint getLastSavepoint() {
    return lastSavepoint;
  }

  public void setLastSavepoint(Savepoint lastSavepoint) {
    this.lastSavepoint = lastSavepoint;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public Long getTriggerTimestamp() {
    return triggerTimestamp;
  }

  public void setTriggerTimestamp(Long triggerTimestamp) {
    this.triggerTimestamp = triggerTimestamp;
  }

  public SavepointTriggerType getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(SavepointTriggerType triggerType) {
    this.triggerType = triggerType;
  }

  public SavepointFormatType getFormatType() {
    return formatType;
  }

  public void setFormatType(SavepointFormatType formatType) {
    this.formatType = formatType;
  }

  public List<Savepoint> getSavepointHistory() {
    return savepointHistory;
  }

  public void setSavepointHistory(List<Savepoint> savepointHistory) {
    this.savepointHistory = savepointHistory;
  }

  public long getLastPeriodicSavepointTimestamp() {
    return lastPeriodicSavepointTimestamp;
  }

  public void setLastPeriodicSavepointTimestamp(long lastPeriodicSavepointTimestamp) {
    this.lastPeriodicSavepointTimestamp = lastPeriodicSavepointTimestamp;
  }

  public void setTrigger(
      String triggerId, SavepointTriggerType triggerType, SavepointFormatType formatType) {
    this.triggerId = triggerId;
    this.triggerTimestamp = System.currentTimeMillis();
    this.triggerType = triggerType;
    this.formatType = formatType;
  }

  public void resetTrigger() {
    this.triggerId = null;
    this.triggerTimestamp = null;
    this.triggerType = null;
    this.formatType = null;
  }

  /**
   * Update last savepoint info and add the savepoint to the history if it isn't already the most
   * recent savepoint.
   *
   * @param savepoint Savepoint to be added.
   */
  public void updateLastSavepoint(Savepoint savepoint) {
    if (lastSavepoint == null || !lastSavepoint.getLocation().equals(savepoint.getLocation())) {
      lastSavepoint = savepoint;
      savepointHistory.add(savepoint);
      if (savepoint.getTriggerType() == SavepointTriggerType.PERIODIC) {
        lastPeriodicSavepointTimestamp = savepoint.getTimeStamp();
      }
    }
    resetTrigger();
  }
}
