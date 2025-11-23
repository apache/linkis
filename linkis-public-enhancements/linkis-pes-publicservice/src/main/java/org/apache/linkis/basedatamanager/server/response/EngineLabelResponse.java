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

package org.apache.linkis.basedatamanager.server.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** A response of engine label list query */
@ApiModel(value = "engine list response", description = "A response of engine label list query")
public class EngineLabelResponse implements Serializable {
  private static final long serialVersionUID = 8326446645908746848L;

  @ApiModelProperty(value = "label id.")
  private Integer labelId;

  @ApiModelProperty(value = "engine name. eg: spark-2.4.3")
  private String engineName;

  @ApiModelProperty(value = "install. eg: yes")
  private String install;

  public EngineLabelResponse(Integer labelId, String engineName, String install) {
    this.labelId = labelId;
    this.engineName = engineName;
    this.install = install;
  }

  public Integer getLabelId() {
    return labelId;
  }

  public void setLabelId(Integer labelId) {
    this.labelId = labelId;
  }

  public String getEngineName() {
    return engineName;
  }

  public void setEngineName(String engineName) {
    this.engineName = engineName;
  }

  public String getInstall() {
    return install;
  }

  public void setInstall(String install) {
    this.install = install;
  }
}
