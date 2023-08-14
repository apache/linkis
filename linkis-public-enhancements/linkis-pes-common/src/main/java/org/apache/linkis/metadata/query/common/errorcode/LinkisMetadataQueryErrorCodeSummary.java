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

package org.apache.linkis.metadata.query.common.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisMetadataQueryErrorCodeSummary implements LinkisErrorCode {
  INVOKE_METHOD_FAIL(199604, "Invoke method:{0} fail,message:{1}(调用方法：{0} 失败,信息：{1})"),
  FAILED_METADATA_SERVICE(199611, "Failed to load metadata service(加载元数据服务失败)"),
  METARUNTIME_EXCEPTION_ID(99900, ""),
  FAIL_CLOSE_CONNECTION(99900, "Fail to close connection(关闭连接失败),"),
  ERROR_IN_CREATING(99900, "Error in creating classloader of type:{0}(创建类型的类加载器时出错)"),
  INIT_META_SERVICE(
      99900, "Fail to init and load meta service class for type:{0}(无法为以下类型初始化和加载元服务类)"),
  NO_CONSTRUCTOR_SERVICE(99900, "No public constructor in meta service class:{0}(元服务类中没有公共构造函数)"),
  UNABLE_META_SERVICE(99900, "Unable to construct meta service class:{0}(无法构建元服务类)"),
  ILLEGAL_META_SERVICE(
      99900, "Illegal arguments in constructor of meta service class:{0}(元服务类的构造函数中的非法参数)"),
  FAIL_DOWNLOAD_RESOURCE(99900, "Fail to download resource(无法下载资源 )"),
  CANNOT_KEYTAB_PARAMETERS(
      99900, "Cannot find the keytab file in connect parameters(在连接参数中找不到密钥表文件)"),
  CANNOT_PARSE_PARAM(99900, "Cannot parse the param:{0}(无法解析参数)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisMetadataQueryErrorCodeSummary(int errorCode, String errorDesc) {
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
