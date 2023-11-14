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

package org.apache.linkis.manager.engineplugin.hbase.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum HBaseErrorCodeSummary implements LinkisErrorCode {
  KERBEROS_PRINCIPAL_NOT_NULL(
      27000,
      "In the hbase authentication mode of kerberos, the kerberos principal cannot be empty(kerberos的hbase认证方式下，kerberos principal不能为空)"),
  KERBEROS_KEYTAB_NOT_NULL(
      27001,
      "In the hbase authentication mode of kerberos, the kerberos keytab cannot be empty(kerberos的hbase认证方式下，kerberos keytab不能为空)"),
  KERBEROS_KEYTAB_FILE_NOT_EXISTS(
      27002, "The kerberos keytab file must exists(kerberos keytab文件必须存在)"),
  KERBEROS_KEYTAB_NOT_FILE(
      27003, "The kerberos keytab file must be a file(kerberos keytab文件必须是个文件)"),
  KERBEROS_AUTH_FAILED(27004, "kerberos authentication failed(kerberos 认证失败)"),
  REGION_SERVER_KERBEROS_PRINCIPAL_NOT_NULL(
      27005,
      "In the hbase authentication mode of kerberos, the region server kerberos principal cannot be empty(kerberos的hbase认证方式下，region server kerberos principal不能为空)"),
  MASTER_KERBEROS_PRINCIPAL_NOT_NULL(
      27006,
      "In the hbase authentication mode of kerberos, the hmaster kerberos principal cannot be empty(kerberos的hbase认证方式下，hmaster kerberos principal不能为空)"),
  HBASE_CLIENT_CONN_CREATE_FAILED(
      27007, "HBase client connection failed to be created(HBase客户端连接创建失败)"),
  HBASE_SHELL_ENV_INIT_FAILED(
      27008, "HBase shell environment initialization failed(HBase shell环境初始化失败)");

  private final int errorCode;

  private final String errorDesc;

  HBaseErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 26000, 29999);
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
