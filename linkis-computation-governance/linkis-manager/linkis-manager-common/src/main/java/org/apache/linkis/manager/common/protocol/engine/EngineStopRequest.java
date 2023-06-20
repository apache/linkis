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
import org.apache.linkis.protocol.message.RequestMethod;

public class EngineStopRequest implements EngineRequest, RequestMethod {

  private ServiceInstance serviceInstance;
  private String logDirSuffix;
  private String engineType;
  private String user;
  /** identifierType, Reserved for ec containerized startup scenarios */
  private String identifierType;
  /** identifier */
  private String identifier;

  public EngineStopRequest() {}

  public EngineStopRequest(ServiceInstance serviceInstance, String user) {
    this.serviceInstance = serviceInstance;
    this.user = user;
  }

  public ServiceInstance getServiceInstance() {
    return serviceInstance;
  }

  public void setServiceInstance(ServiceInstance serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  public String getLogDirSuffix() {
    return logDirSuffix;
  }

  public void setLogDirSuffix(String logDirSuffix) {
    this.logDirSuffix = logDirSuffix;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getIdentifierType() {
    return identifierType;
  }

  public void setIdentifierType(String identifierType) {
    this.identifierType = identifierType;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public String method() {
    return "/engine/stop";
  }

  @Override
  public String toString() {
    return "EngineStopRequest{"
        + "serviceInstance="
        + serviceInstance
        + ", logDirSuffix='"
        + logDirSuffix
        + '\''
        + ", engineType='"
        + engineType
        + '\''
        + ", user='"
        + user
        + '\''
        + ", identifierType='"
        + identifierType
        + '\''
        + ", identifier='"
        + identifier
        + '\''
        + '}';
  }
}
