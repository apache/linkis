/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
      26010,
      "Failed to get datasource info from datasource server(从数据源服务器获取数据源信息失败)",
      "Unable to get datasource information from datasource server due to some error(由于某些错误，无法从数据源服务器获取数据源信息)",
      "jdbcEngineConnExecutor"),
  JDBC_PARAMS_ILLEGAL(
      26011,
      "JDBC related parameters are illegal(JDBC相关参数非法)",
      "JDBC related parameters do not meet the requirements, may be empty or not set, etc(jdbc相关参数不符合要求，可能为空或者没有设置等)",
      "jdbc"),
  DRIVER_CLASS_NAME_ERROR(
      26012,
      "The driver class name is required(驱动程序类名是必需的)",
      "Driver class name must be set(必须设置驱动程序类名)",
      "connectionManager"),
  UNSUPPORT_JDBC_AUTHENTICATION_TYPES(
      26013,
      "Unsupported jdbc authentication types(不支持的 jdbc 身份验证类型)",
      "Some jdbc types of authentication are not supported(某些jdbc类型的身份认证不支持)",
      "connectionManager"),
  NO_EXEC_USER_ERROR(26014, "No execUser(没有执行用户参数)", "No execUser(没有执行用户参数)", "connectionManager"),
  GET_CURRENT_USER_ERROR(
      26015,
      "Error in getCurrentUser(获取当前用户出错)",
      "Error getting current user in some cases(某些情况下获取当前用户出错)",
      "connectionManager"),
  DOAS_FOR_GET_CONNECTION_ERROR(
      26016,
      "Error in doAs to get one connection(doAs 获取一个连接时出错)",
      "Error getting a connection via doAs(通过doAs 的方式获取一个连接时出错)",
      "connectionManager"),
  JDBC_USERNAME_NOT_EMPTY(
      26017,
      "The jdbc username is not empty(jdbc用户名不为空)",
      "The username of jdbc must be set(必须设置 jdbc的username)",
      "jdbcParamUtils"),
  JDBC_PASSWORD_NOT_EMPTY(
      26018,
      "The jdbc password is not empty(jdbc密码不为空)",
      "The password of jdbc must be set(必须设置 jdbc的password)",
      "jdbcParamUtils"),
  DATA_SOURCE_INFO_NOT_FOUND(
      26019,
      "Data source info not found(数据源信息未找到)",
      "Data source info not found(数据源信息未找到)",
      "jdbcMultiDatasourceParser"),
  DATA_SOURCE_NOT_PUBLISHED(
      26020,
      "Data source not yet published(数据源尚未发布)",
      "Data source not yet published(数据源尚未发布)",
      "jdbcMultiDatasourceParser"),
  DATA_SOURCE_EXPIRED(
      26021,
      "Data source is expired(数据源已过期)",
      "Data source is expired(数据源已过期)",
      "jdbcMultiDatasourceParser"),
  DATA_SOURCE_JDBC_TYPE_NOT_NULL(
      26022,
      "The data source jdbc type cannot be null(数据源 jdbc 类型不能为空)",
      "The data source jdbc type cannot be null(数据源 jdbc 类型不能为空)",
      "jdbcMultiDatasourceParser"),
  JDBC_CONNECTION_INFO_NOT_NULL(
      26023,
      "The data source jdbc connection info cannot be null(数据源 jdbc 连接信息不能为空)",
      "The data source jdbc connection info cannot be null(数据源 jdbc 连接信息不能为空)",
      "jdbcMultiDatasourceParser"),
  JDBC_DRIVER_CLASS_NAME_NOT_NULL(
      26024,
      "The data source jdbc driverClassName cannot be null(数据源 jdbc driverClassName 不能为空)",
      "The data source jdbc driverClassName cannot be null(数据源 jdbc driverClassName 不能为空)",
      "jdbcMultiDatasourceParser"),
  JDBC_HOST_NOT_NULL(
      26025,
      "The data source jdbc connection host cannot be null(数据源jdbc连接主机不能为空)",
      "The data source jdbc connection host cannot be null(数据源jdbc连接主机不能为空)",
      "jdbcMultiDatasourceParser"),
  JDBC_PORT_NOT_NULL(
      26026,
      "The data source jdbc connection port cannot be null(数据源jdbc连接端口不能为空)",
      "The data source jdbc connection port cannot be null(数据源jdbc连接端口不能为空)",
      "jdbcMultiDatasourceParser"),
  KERBEROS_PRINCIPAL_NOT_NULL(
      26027,
      "In the jdbc authentication mode of kerberos, the kerberos principal cannot be empty(kerberos的jdbc认证方式下，kerberos principal不能为空)",
      "In the jdbc authentication mode of kerberos, the kerberos principal cannot be empty(kerberos的jdbc认证方式下，kerberos principal不能为空)",
      "jdbcMultiDatasourceParser"),
  KERBEROS_KEYTAB_NOT_NULL(
      26028,
      "In the jdbc authentication mode of kerberos, the kerberos keytab cannot be empty(kerberos的jdbc认证方式下，kerberos keytab不能为空)",
      "In the jdbc authentication mode of kerberos, the kerberos keytab cannot be empty(kerberos的jdbc认证方式下，kerberos keytab不能为空)",
      "jdbcMultiDatasourceParser"),
  UNSUPPORTED_AUTHENTICATION_TYPE(
      26029,
      "Unsupported authentication type(不支持的身份验证类型)",
      "Some authentication types are not supported(Some authentication types are not supported)",
      "jdbcMultiDatasourceParser");

  private int errorCode;

  private String errorDesc;

  private String comment;

  private String module;

  JDBCErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
    ErrorCodeUtils.validateErrorCode(errorCode, 26000, 29999);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
    this.module = module;
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

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
