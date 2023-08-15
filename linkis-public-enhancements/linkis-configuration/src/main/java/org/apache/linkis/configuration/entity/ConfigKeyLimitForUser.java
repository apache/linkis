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

package org.apache.linkis.configuration.entity;

import java.util.Date;

/** for table linkis_ps_configuration_key_limit_for_user @Description */
public class ConfigKeyLimitForUser {

  /** Table field: id Field type: bigint(19) */
  private Long id;

  /** Username table field: user_name field type: varchar(50) */
  private String userName;

  /**
   * combined label combined_userCreator_engineType such as hadoop-IDE, spark-2.4.3 table field:
   * combined_label_value field type: varchar(200)
   */
  private String combinedLabelValue;

  /** id of linkis_ps_configuration_config_key table field: key_id field type: bigint(19) */
  private Long keyId;

  /** Configuration value table field: config_value field type: varchar(200) */
  private String configValue;

  /** Upper limit table field: max_value field type: varchar(50) */
  private String maxValue;

  /** Lower limit value (reserved) table field: min_value field type: varchar(50) */
  private String minValue;

  /**
   * uuid The template id table field of the third-party record: latest_update_template_uuid Field
   * type: varchar(34)
   */
  private String latestUpdateTemplateUuid;

  /** Is it valid Reserved Y/N table field: is_valid field type: varchar(2) */
  private String isValid;

  /** Creator table field: create_by field type: varchar(50) */
  private String createBy;

  /**
   * create time table field: create_time field type: timestamp(19) default value: CURRENT_TIMESTAMP
   */
  private Date createTime;

  /** Updater table field: update_by field type: varchar(50) */
  private String updateBy;

  /**
   * update time table field: update_time field type: timestamp(19) default value: CURRENT_TIMESTAMP
   */
  private Date updateTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getCombinedLabelValue() {
    return combinedLabelValue;
  }

  public void setCombinedLabelValue(String combinedLabelValue) {
    this.combinedLabelValue = combinedLabelValue;
  }

  public Long getKeyId() {
    return keyId;
  }

  public void setKeyId(Long keyId) {
    this.keyId = keyId;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  public String getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  public String getMinValue() {
    return minValue;
  }

  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  public String getLatestUpdateTemplateUuid() {
    return latestUpdateTemplateUuid;
  }

  public void setLatestUpdateTemplateUuid(String latestUpdateTemplateUuid) {
    this.latestUpdateTemplateUuid = latestUpdateTemplateUuid;
  }

  public String getIsValid() {
    return isValid;
  }

  public void setIsValid(String isValid) {
    this.isValid = isValid;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", userName=").append(userName);
    sb.append(", combinedLabelValue=").append(combinedLabelValue);
    sb.append(", keyId=").append(keyId);
    sb.append(", configValue=").append(configValue);
    sb.append(", maxValue=").append(maxValue);
    sb.append(", minValue=").append(minValue);
    sb.append(", latestUpdateTemplateUuid=").append(latestUpdateTemplateUuid);
    sb.append(", isValid=").append(isValid);
    sb.append(", createBy=").append(createBy);
    sb.append(", createTime=").append(createTime);
    sb.append(", updateBy=").append(updateBy);
    sb.append(", updateTime=").append(updateTime);
    sb.append(']');
    return sb.toString();
  }
}
