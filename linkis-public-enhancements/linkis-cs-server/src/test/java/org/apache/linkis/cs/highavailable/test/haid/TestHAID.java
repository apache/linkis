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

package org.apache.linkis.cs.highavailable.test.haid;

import org.apache.linkis.cs.common.entity.source.HAContextID;

public class TestHAID implements HAContextID {

  private String contextId;
  private String instance;
  private String backupInstance;

  @Override
  public String getContextId() {
    return contextId;
  }

  @Override
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  @Override
  public String getInstance() {
    return instance;
  }

  @Override
  public void setInstance(String instance) {
    this.instance = instance;
  }

  @Override
  public String getBackupInstance() {
    return backupInstance;
  }

  @Override
  public void setBackupInstance(String backupInstance) {
    this.backupInstance = backupInstance;
  }

  @Override
  public HAContextID copy() {
    return null;
  }
}
