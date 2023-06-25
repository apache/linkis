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

/**
 * linkis_ps_configuration_key_limit_for_user表的entity类 @Description
 *
 * @version 1.0
 * @author webank
 * @see <a>http://10.107.99.84:53661/wego-rad/site/plugin/weup-tool/mybatisGen.html</a>
 * @see <a>http://km.weoa.com/group/wesmart/article/10467</a>
 */
public class ConfigKeyLimitForUser {

  /** 表字段 : id 字段类型 : bigint(19) */
  private Long id;

  /** 用户名 表字段 : user_name 字段类型 : varchar(50) */
  private String userName;

  /**
   * 组合标签 combined_userCreator_engineType 如 hadoop-IDE,spark-2.4.3 表字段 : combined_label_value 字段类型 :
   * varchar(200)
   */
  private String combinedLabelValue;

  /** id of linkis_ps_configuration_config_key 表字段 : key_id 字段类型 : bigint(19) */
  private Long keyId;

  /** uuid 第三方侧记录的模板id 表字段 : latest_update_template_uuid 字段类型 : varchar(34) */
  private String latestUpdateTemplateUuid;

  /** 是否有效 预留 Y/N 表字段 : is_valid 字段类型 : varchar(2) */
  private String isValid;

  /** 创建人 表字段 : create_by 字段类型 : varchar(50) */
  private String createBy;

  /** create time 表字段 : create_time 字段类型 : timestamp(19) 默认值 : CURRENT_TIMESTAMP */
  private Date createTime;

  /** 更新人 表字段 : update_by 字段类型 : varchar(50) */
  private String updateBy;

  /** update time 表字段 : update_time 字段类型 : timestamp(19) 默认值 : CURRENT_TIMESTAMP */
  private Date updateTime;

  /**
   * 获取：<br>
   * 字段：id
   *
   * @return
   */
  public Long getId() {
    return id;
  }

  /**
   * 设置：<br>
   * 字段：id
   *
   * @param
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * 获取：用户名<br>
   * 字段：user_name
   *
   * @return
   */
  public String getUserName() {
    return userName;
  }

  /**
   * 设置：用户名<br>
   * 字段：user_name
   *
   * @param
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * 获取：组合标签 combined_userCreator_engineType 如 hadoop-IDE,spark-2.4.3<br>
   * 字段：combined_label_value
   *
   * @return
   */
  public String getCombinedLabelValue() {
    return combinedLabelValue;
  }

  /**
   * 设置：组合标签 combined_userCreator_engineType 如 hadoop-IDE,spark-2.4.3<br>
   * 字段：combined_label_value
   *
   * @param
   */
  public void setCombinedLabelValue(String combinedLabelValue) {
    this.combinedLabelValue = combinedLabelValue;
  }

  /**
   * 获取：id of linkis_ps_configuration_config_key<br>
   * 字段：key_id
   *
   * @return
   */
  public Long getKeyId() {
    return keyId;
  }

  /**
   * 设置：id of linkis_ps_configuration_config_key<br>
   * 字段：key_id
   *
   * @param
   */
  public void setKeyId(Long keyId) {
    this.keyId = keyId;
  }

  /**
   * 获取：uuid 第三方侧记录的模板id<br>
   * 字段：latest_update_template_uuid
   *
   * @return
   */
  public String getLatestUpdateTemplateUuid() {
    return latestUpdateTemplateUuid;
  }

  /**
   * 设置：uuid 第三方侧记录的模板id<br>
   * 字段：latest_update_template_uuid
   *
   * @param
   */
  public void setLatestUpdateTemplateUuid(String latestUpdateTemplateUuid) {
    this.latestUpdateTemplateUuid = latestUpdateTemplateUuid;
  }

  /**
   * 获取：是否有效 预留 Y/N<br>
   * 字段：is_valid
   *
   * @return
   */
  public String getIsValid() {
    return isValid;
  }

  /**
   * 设置：是否有效 预留 Y/N<br>
   * 字段：is_valid
   *
   * @param
   */
  public void setIsValid(String isValid) {
    this.isValid = isValid;
  }

  /**
   * 获取：创建人<br>
   * 字段：create_by
   *
   * @return
   */
  public String getCreateBy() {
    return createBy;
  }

  /**
   * 设置：创建人<br>
   * 字段：create_by
   *
   * @param
   */
  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  /**
   * 获取：create time<br>
   * 字段：create_time
   *
   * @return
   */
  public Date getCreateTime() {
    return createTime;
  }

  /**
   * 设置：create time<br>
   * 字段：create_time
   *
   * @param
   */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /**
   * 获取：更新人<br>
   * 字段：update_by
   *
   * @return
   */
  public String getUpdateBy() {
    return updateBy;
  }

  /**
   * 设置：更新人<br>
   * 字段：update_by
   *
   * @param
   */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  /**
   * 获取：update time<br>
   * 字段：update_time
   *
   * @return
   */
  public Date getUpdateTime() {
    return updateTime;
  }

  /**
   * 设置：update time<br>
   * 字段：update_time
   *
   * @param
   */
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
    sb.append(", latestUpdateTemplateUuid=").append(latestUpdateTemplateUuid);
    sb.append(", isValid=").append(isValid);
    sb.append(", createBy=").append(createBy);
    sb.append(", createTime=").append(createTime);
    sb.append(", updateBy=").append(updateBy);
    sb.append(", updateTime=").append(updateTime);
    sb.append(']');
    return sb.toString();
  }

  // === 下方为用户自定义模块,下次生成会保留 ===
}
