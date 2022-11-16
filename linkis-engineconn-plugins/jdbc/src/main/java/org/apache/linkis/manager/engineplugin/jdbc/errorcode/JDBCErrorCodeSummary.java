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

package org.apache.linkis.manager.engineplugin.jdbc.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;

public enum JDBCErrorCodeSummary {
  /**
   * 10000-10999 linkis-frame 11000-12999 linkis-commons 13000-14999 linkis-spring-cloud-services
   * 15000-19999 linkis-public-enhancements 20000-24999 linkis-computation-governance 25000-25999
   * linkis-extensions 26000-29999 linkis-engineconn-plugins
   */
  JDBC_GET_DATASOURCEINFO_ERROR(
      26010, "Failed to get datasource info from datasource server(从数据源服务器获取数据源信息失败)"),
  JDBC_PARAMS_ILLEGAL(26011, "JDBC related parameters are illegal(JDBC 相关参数非法)"),
  DRIVER_CLASS_NAME_ERROR(26012, "The driver class name is required(驱动程序类名是必需的)"),
  UNSUPPORT_JDBC_AUTHENTICATION_TYPES(
      26013, "Unsupported jdbc authentication types:{0}(不支持的 jdbc 身份验证类型:{0})"),
  NO_EXEC_USER_ERROR(26014, "execUser is empty (execUser 为空)"),
  GET_CURRENT_USER_ERROR(26015, "Error in getCurrentUser(获取当前用户出错)"),
  DOAS_FOR_GET_CONNECTION_ERROR(26016, "Error in doAs to get one connection(执行 doAs 获取一个连接时出错)"),
  JDBC_USERNAME_NOT_EMPTY(26017, "The jdbc username cannot be empty(jdbc 用户名不能为空)"),
  JDBC_PASSWORD_NOT_EMPTY(26018, "The jdbc password cannot be empty(jdbc 密码不能为空)"),
  DATA_SOURCE_INFO_NOT_FOUND(
      26019, "Data source info of datasource name:[{0}] not found(数据源：[{0}] 信息未找到)"),
  DATA_SOURCE_NOT_PUBLISHED(
      26020, "Data source of datasource name:[{0}] not yet published(数据源：[{0}]尚未发布)"),
  DATA_SOURCE_EXPIRED(26021, "Data source of datasource name:[{0}] is expired(数据源：[{0}]已过期)"),
  DATA_SOURCE_JDBC_TYPE_NOT_NULL(
      26022, "The data source jdbc type cannot be null(数据源 jdbc 类型不能为空)"),
  JDBC_CONNECTION_INFO_NOT_NULL(
      26023, "The data source jdbc connection info cannot be null(数据源 jdbc 连接信息不能为空)"),
  JDBC_DRIVER_CLASS_NAME_NOT_NULL(
      26024, "The data source jdbc driverClassName cannot be null(数据源 jdbc driverClassName 不能为空)"),
  JDBC_HOST_NOT_NULL(
      26025, "The data source jdbc connection host cannot be null(数据源 jdbc 连接主机不能为空)"),
  JDBC_PORT_NOT_NULL(26026, "The data source jdbc connection port cannot be null(数据源jdbc连接端口不能为空)"),
  KERBEROS_PRINCIPAL_NOT_NULL(
      26027,
      "In the jdbc authentication mode of kerberos, the kerberos principal cannot be empty(kerberos的jdbc认证方式下，kerberos principal不能为空)"),
  KERBEROS_KEYTAB_NOT_NULL(
      26028,
      "In the jdbc authentication mode of kerberos, the kerberos keytab cannot be empty(kerberos的jdbc认证方式下，kerberos keytab不能为空)"),
  UNSUPPORTED_AUTHENTICATION_TYPE(26029, "Unsupported authentication type:{0}(不支持的身份验证类型)");

  private int errorCode;

  private String errorDesc;

  JDBCErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 26000, 29999);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorDesc() {
    return errorDesc;
  }

  public void setErrorDesc(String errorDesc) {
    this.errorDesc = errorDesc;
  }

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
