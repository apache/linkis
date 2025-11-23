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

package org.apache.linkis.basedatamanager.server.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("linkis_cg_manager_label")
public class CgManagerLabel implements Serializable {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  private String labelKey;

  private String labelValue;

  private String labelFeature;

  private Integer labelValueSize;

  private LocalDateTime updateTime;

  private LocalDateTime createTime;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLabelKey() {
    return labelKey;
  }

  public void setLabelKey(String labelKey) {
    this.labelKey = labelKey;
  }

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(String labelValue) {
    this.labelValue = labelValue;
  }

  public String getLabelFeature() {
    return labelFeature;
  }

  public void setLabelFeature(String labelFeature) {
    this.labelFeature = labelFeature;
  }

  public Integer getLabelValueSize() {
    return labelValueSize;
  }

  public void setLabelValueSize(Integer labelValueSize) {
    this.labelValueSize = labelValueSize;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  @Override
  public String toString() {
    return "LinkisCgManagerLabel{"
        + "id="
        + id
        + ", labelKey="
        + labelKey
        + ", labelValue="
        + labelValue
        + ", labelFeature="
        + labelFeature
        + ", labelValueSize="
        + labelValueSize
        + ", updateTime="
        + updateTime
        + ", createTime="
        + createTime
        + "}";
  }
}
