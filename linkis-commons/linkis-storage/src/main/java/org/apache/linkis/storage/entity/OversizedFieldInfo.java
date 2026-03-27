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

/** Represents information about an oversized field in a result set */
public class OversizedFieldInfo {
  private String fieldName;
  private Integer rowIndex;
  private Integer actualLength;
  private Integer maxLength;

  public OversizedFieldInfo() {}

  public OversizedFieldInfo(
      String fieldName, Integer rowIndex, Integer actualLength, Integer maxLength) {
    this.fieldName = fieldName;
    this.rowIndex = rowIndex;
    this.actualLength = actualLength;
    this.maxLength = maxLength;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Integer getRowIndex() {
    return rowIndex;
  }

  public void setRowIndex(Integer rowIndex) {
    this.rowIndex = rowIndex;
  }

  public Integer getActualLength() {
    return actualLength;
  }

  public void setActualLength(Integer actualLength) {
    this.actualLength = actualLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }
}
