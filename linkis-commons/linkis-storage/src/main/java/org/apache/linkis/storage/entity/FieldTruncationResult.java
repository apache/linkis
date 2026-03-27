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

package org.apache.linkis.storage.entity;

import java.util.List;

/** Result of field truncation detection and processing */
public class FieldTruncationResult {
  private boolean hasOversizedFields;
  private List<OversizedFieldInfo> oversizedFields;
  private Integer maxOversizedFieldCount;
  private List<String[]> data;

  public FieldTruncationResult() {}

  public FieldTruncationResult(
      boolean hasOversizedFields,
      List<OversizedFieldInfo> oversizedFields,
      Integer maxOversizedFieldCount,
      List<String[]> data) {
    this.hasOversizedFields = hasOversizedFields;
    this.oversizedFields = oversizedFields;
    this.maxOversizedFieldCount = maxOversizedFieldCount;
    this.data = data;
  }

  public boolean isHasOversizedFields() {
    return hasOversizedFields;
  }

  public void setHasOversizedFields(boolean hasOversizedFields) {
    this.hasOversizedFields = hasOversizedFields;
  }

  public List<OversizedFieldInfo> getOversizedFields() {
    return oversizedFields;
  }

  public void setOversizedFields(List<OversizedFieldInfo> oversizedFields) {
    this.oversizedFields = oversizedFields;
  }

  public Integer getMaxOversizedFieldCount() {
    return maxOversizedFieldCount;
  }

  public void setMaxOversizedFieldCount(Integer maxOversizedFieldCount) {
    this.maxOversizedFieldCount = maxOversizedFieldCount;
  }

  public List<String[]> getData() {
    return data;
  }

  public void setData(List<String[]> data) {
    this.data = data;
  }
}
