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

package org.apache.linkis.manager.common.protocol.em;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.io.Serializable;
import java.util.Map;

public class RegisterEMRequest implements EMRequest, RequestProtocol, Serializable {

  private ServiceInstance serviceInstance = null;

  private Map<String, Object> labels = null;

  private NodeResource nodeResource = null;

  private String user = null;

  private String alias = null;

  public ServiceInstance getServiceInstance() {
    return serviceInstance;
  }

  public void setServiceInstance(ServiceInstance serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  public Map<String, Object> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, Object> labels) {
    this.labels = labels;
  }

  public NodeResource getNodeResource() {
    return nodeResource;
  }

  public void setNodeResource(NodeResource nodeResource) {
    this.nodeResource = nodeResource;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getUser() {
    return this.user;
  }
}
