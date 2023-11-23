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

package org.apache.linkis.monitor.instance.entity;

public class InsPersistenceLabelValue {

  private Integer labelId;

  private String valueKey;

  private String valueContent;

  public InsPersistenceLabelValue() {}

  public InsPersistenceLabelValue(Integer labelId, String key, String content) {
    this.labelId = labelId;
    this.valueKey = key;
    this.valueContent = content;
  }

  public String getValueKey() {
    return valueKey;
  }

  public void setValueKey(String valueKey) {
    this.valueKey = valueKey;
  }

  public String getValueContent() {
    return valueContent;
  }

  public void setValueContent(String valueContent) {
    this.valueContent = valueContent;
  }

  public Integer getLabelId() {
    return labelId;
  }

  public void setLabelId(Integer labelId) {
    this.labelId = labelId;
  }
}
