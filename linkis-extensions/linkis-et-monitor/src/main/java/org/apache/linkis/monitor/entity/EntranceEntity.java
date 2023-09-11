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

package org.apache.linkis.monitor.entity;

import java.io.Serializable;

public class EntranceEntity implements Serializable {

  private Integer runningtasks;

  private Integer queuedtasks;

  private String alteruser;

  private String username;

  public Integer getQueuedtasks() {
    return queuedtasks;
  }

  public void setQueuedtasks(Integer queuedtasks) {
    this.queuedtasks = queuedtasks;
  }

  public String getAlteruser() {
    return alteruser;
  }

  public void setAlteruser(String alteruser) {
    this.alteruser = alteruser;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Integer getRunningtasks() {
    return runningtasks;
  }

  public void setRunningtasks(Integer runningtasks) {
    this.runningtasks = runningtasks;
  }
}
