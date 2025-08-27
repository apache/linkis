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

package org.apache.linkis.instance.label.entity;

import org.apache.linkis.manager.label.entity.GenericLabel;

import java.util.Date;

/** like: PersistenceLabel in label-manager-common */
public class InsPersistenceLabel extends GenericLabel {
  private Integer id;
  private int labelValueSize = -1;
  private String stringValue;
  private Boolean modifiable = false;

  private Date updateTime;
  private Date createTime;

  public Boolean getModifiable() {
    return modifiable;
  }

  public void setModifiable(Boolean modifiable) {
    this.modifiable = modifiable;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getLabelValueSize() {
    return labelValueSize;
  }

  public void setLabelValueSize(int labelValueSize) {
    this.labelValueSize = labelValueSize;
  }

  @Override
  public String getStringValue() {
    return stringValue;
  }

  @Override
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public boolean equals(Object other) {
    if (null != this.getLabelKey() && other instanceof InsPersistenceLabel) {
      InsPersistenceLabel otherInsLabel = (InsPersistenceLabel) other;
      if (this.getLabelKey().equals(otherInsLabel.getLabelKey())) {
        return (null == this.getStringValue() && null == otherInsLabel.getStringValue())
            || (null != this.getStringValue()
                && this.getStringValue().equals(otherInsLabel.getStringValue()));
      }
    }
    return false;
  }
}
