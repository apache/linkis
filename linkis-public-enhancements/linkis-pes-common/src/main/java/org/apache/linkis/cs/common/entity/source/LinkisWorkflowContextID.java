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

package org.apache.linkis.cs.common.entity.source;

import org.apache.linkis.cs.common.protocol.ContextIDType;

public class LinkisWorkflowContextID implements WorkflowContextID {

  private String workspace;

  private String project;

  private String flow;

  private String contextId;

  private String version;

  private String env;

  @Override
  public String getWorkSpace() {
    return this.workspace;
  }

  @Override
  public void setWorkSpace(String workSpace) {
    this.workspace = workSpace;
  }

  @Override
  public String getProject() {
    return this.project;
  }

  @Override
  public void setProject(String project) {
    this.project = project;
  }

  @Override
  public String getFlow() {
    return this.flow;
  }

  @Override
  public void setFlow(String flow) {
    this.flow = flow;
  }

  @Override
  public String getVersion() {
    return this.version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getEnv() {
    return this.env;
  }

  @Override
  public void setEnv(String env) {
    this.env = env;
  }

  @Override
  public String getContextId() {
    return this.contextId;
  }

  @Override
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  @Override
  public int getContextIDType() {
    return ContextIDType.LINKIS_WORKFLOW_CONTEXT_ID_TYPE.getIndex();
  }
}
