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

public class CombinedNodeIDContextID extends LinkisHAWorkFlowContextID {

  public CombinedNodeIDContextID() {}

  public CombinedNodeIDContextID(ContextID contextID, String nodeID) {

    this.setContextId(contextID.getContextId());
    this.nodeID = nodeID;
    if (contextID instanceof LinkisHAWorkFlowContextID) {
      LinkisHAWorkFlowContextID haWorkFlowContextID = (LinkisHAWorkFlowContextID) contextID;
      setBackupInstance(haWorkFlowContextID.getBackupInstance());
      setInstance(haWorkFlowContextID.getInstance());
      setEnv(haWorkFlowContextID.getEnv());
      setFlow(haWorkFlowContextID.getFlow());
      setProject(haWorkFlowContextID.getProject());
      setVersion(haWorkFlowContextID.getVersion());
      setWorkSpace(haWorkFlowContextID.getWorkSpace());
      setUser(haWorkFlowContextID.getUser());
    }
  }

  private String nodeID;

  public String getNodeID() {
    return nodeID;
  }

  public void setNodeID(String nodeID) {
    this.nodeID = nodeID;
  }

  public LinkisHAWorkFlowContextID getLinkisHaWorkFlowContextID() {
    LinkisHAWorkFlowContextID linkisHAWorkFlowContextID = new LinkisHAWorkFlowContextID();
    linkisHAWorkFlowContextID.setBackupInstance(getBackupInstance());
    linkisHAWorkFlowContextID.setInstance(getInstance());
    linkisHAWorkFlowContextID.setEnv(getEnv());
    linkisHAWorkFlowContextID.setFlow(getFlow());
    linkisHAWorkFlowContextID.setProject(getProject());
    linkisHAWorkFlowContextID.setVersion(getVersion());
    linkisHAWorkFlowContextID.setWorkSpace(getWorkSpace());
    linkisHAWorkFlowContextID.setUser(getUser());
    linkisHAWorkFlowContextID.setContextId(getContextId());
    return linkisHAWorkFlowContextID;
  }

  @Override
  public HAContextID copy() {
    CombinedNodeIDContextID contextID = new CombinedNodeIDContextID();
    contextID.setBackupInstance(getBackupInstance());
    contextID.setInstance(getInstance());
    contextID.setEnv(getEnv());
    contextID.setFlow(getFlow());
    contextID.setProject(getProject());
    contextID.setVersion(getVersion());
    contextID.setWorkSpace(getWorkSpace());
    contextID.setUser(getUser());
    contextID.setContextId(getContextId());
    contextID.setNodeID(nodeID);
    return contextID;
  }
}
