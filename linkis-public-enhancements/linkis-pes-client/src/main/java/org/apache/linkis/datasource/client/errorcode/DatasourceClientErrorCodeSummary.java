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

package org.apache.linkis.datasource.client.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum DatasourceClientErrorCodeSummary implements LinkisErrorCode {
  CLIENT_ERROR_CODE(31000, ""),
  SERVERURL_CANNOT_NULL(31000, "serverUrl cannot be null.(serverUrl 不能为空.)"),
  VERSION_NEEDED(31000, "version is needed(版本为空)!"),
  VERSIONID_NEEDED(31000, "version is needed(需要版本ID)!"),
  DATASOURCEID_NEEDED(31000, "dataSourceId is needed(需要dataSourceId)!"),
  DATASOURCENAME_NEEDED(31000, "dataSourceName is needed(需要dataSourceName)!"),
  USER_NEEDED(31000, "user is needed(用户为空)!"),
  SYSTEM_NEEDED(31000, "system is needed(系统为空)!"),
  IP_NEEDED(31000, "ip is needed(ip为空)!"),
  PORT_NEEDED(31000, "port is needed(port为空)!"),
  OWNER_NEEDED(31000, "owner is needed(owner为空)!"),
  DATASOURCE_NEEDED(31000, "datasourceTypeName is needed(datasourceTypeName为空)!"),
  CANNOT_SOURCE(
      31000, "Cannot encode the name of data source:{0} for request(无法对请求的数据源名称进行编码：{0})"),
  DATABASE_NEEDED(31000, "database is needed(数据库为空)!"),
  TABLE_NEEDED(31000, "table is needed(表为空)!");
  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  DatasourceClientErrorCodeSummary(int errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  @Override
  public int getErrorCode() {
    return errorCode;
  }

  @Override
  public String getErrorDesc() {
    return errorDesc;
  }
}
