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

package org.apache.linkis.manager.common.protocol.resource;

import org.apache.linkis.protocol.AbstractRetryableProtocol;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.HashMap;
import java.util.Map;

public class ResponseTaskRunningInfo extends AbstractRetryableProtocol implements RequestProtocol {
  private final String execId;
  private final float progress;
  private final JobProgressInfo[] progressInfo;
  private final Map<String, ResourceWithStatus> resourceMap;
  private final Map<String, Object> extraInfoMap;

  public ResponseTaskRunningInfo(
      String execId,
      float progress,
      JobProgressInfo[] progressInfo,
      Map<String, ResourceWithStatus> resourceMap,
      Map<String, Object> extraInfoMap) {
    this.execId = execId;
    this.progress = progress;
    this.progressInfo = progressInfo;
    if (resourceMap != null) {
      this.resourceMap = new HashMap<>(resourceMap);
    } else {
      this.resourceMap = new HashMap<>();
    }
    if (extraInfoMap != null) {
      this.extraInfoMap = new HashMap<>(extraInfoMap);
    } else {
      this.extraInfoMap = new HashMap<>();
    }
  }

  public String getExecId() {
    return execId;
  }

  public float getProgress() {
    return progress;
  }

  public JobProgressInfo[] getProgressInfo() {
    return progressInfo;
  }

  public HashMap<String, ResourceWithStatus> getResourceMap() {
    return new HashMap<>(resourceMap);
  }

  public HashMap<String, Object> getExtraInfoMap() {
    return new HashMap<>(extraInfoMap);
  }
}
