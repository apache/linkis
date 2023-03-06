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

package org.apache.linkis.engineplugin.spark.datacalc.model;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class SourceConfig extends DataCalcPluginConfig
    implements ResultTableConfig, Serializable {

  @NotBlank protected String resultTable;

  private Boolean persist = false;

  private String storageLevel = "MEMORY_AND_DISK";

  protected Map<String, String> options = new HashMap<>();

  public String getResultTable() {
    return resultTable;
  }

  public void setResultTable(String resultTable) {
    this.resultTable = resultTable;
  }

  public Boolean getPersist() {
    return persist;
  }

  public void setPersist(Boolean persist) {
    this.persist = persist;
  }

  public String getStorageLevel() {
    return storageLevel;
  }

  public void setStorageLevel(String storageLevel) {
    if (StringUtils.isNotBlank(storageLevel)) this.storageLevel = storageLevel;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
