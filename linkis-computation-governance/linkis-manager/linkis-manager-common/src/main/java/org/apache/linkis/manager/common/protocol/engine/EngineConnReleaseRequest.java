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

package org.apache.linkis.manager.common.protocol.engine;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;

public class EngineConnReleaseRequest implements EngineRequest {

  private ServiceInstance serviceInstance;

  private String user;

  private String msg;

  private NodeStatus nodeStatus;

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  private String ticketId;

  public EngineConnReleaseRequest() {}

  public EngineConnReleaseRequest(
      ServiceInstance serviceInstance, String user, String msg, String ticketId) {
    this.serviceInstance = serviceInstance;
    this.user = user;
    this.msg = msg;
    this.ticketId = ticketId;
  }

  @Override
  public String getUser() {
    return this.user;
  }

  public ServiceInstance getServiceInstance() {
    return serviceInstance;
  }

  public void setServiceInstance(ServiceInstance serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public NodeStatus getNodeStatus() {
    return nodeStatus;
  }

  public void setNodeStatus(NodeStatus nodeStatus) {
    this.nodeStatus = nodeStatus;
  }
}
