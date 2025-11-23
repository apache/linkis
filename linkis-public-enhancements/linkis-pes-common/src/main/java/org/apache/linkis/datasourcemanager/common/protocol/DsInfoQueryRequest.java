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

package org.apache.linkis.datasourcemanager.common.protocol;

import org.apache.linkis.protocol.message.RequestProtocol;

import org.apache.commons.lang3.StringUtils;

public class DsInfoQueryRequest implements RequestProtocol {
  private String id;
  private String name;
  private String system;
  private String envId;

  public DsInfoQueryRequest(String id, String name, String system, String envId) {
    this.id = id;
    this.name = name;
    this.system = system;
    this.envId = envId;
  }

  public DsInfoQueryRequest(String id, String name, String system) {
    this.id = id;
    this.name = name;
    this.system = system;
  }

  public boolean isValid() {
    return (StringUtils.isNotBlank(id) || StringUtils.isNotBlank(name))
        && StringUtils.isNotBlank(system);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSystem() {
    return system;
  }

  public String getEnvId() {
    return envId;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return id
   */
  @Deprecated
  public String id() {
    return this.id;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return name
   */
  @Deprecated
  public String name() {
    return this.name;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return system
   */
  @Deprecated
  public String system() {
    return this.system;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return envId
   */
  @Deprecated
  public String envId() {
    return this.envId;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public void setEnvId(String envId) {
    this.envId = envId;
  }
}
