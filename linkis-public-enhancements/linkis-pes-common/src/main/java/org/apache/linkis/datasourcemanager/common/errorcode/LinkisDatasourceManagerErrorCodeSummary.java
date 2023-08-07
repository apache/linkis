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

package org.apache.linkis.datasourcemanager.common.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisDatasourceManagerErrorCodeSummary implements LinkisErrorCode {
  SERIALIZATION_FAILED(16897, "Unable to deserialize to object from string(json) in type: (序列化失败)"),
  CANNOT_BE_SERIALIZATION(16898, "cannot be serialized (无法序列化)"),
  CONNECTION_FAILED(99983, "Connection Failed(连接失败)"),
  REMOTE_SERVICE_ERROR(99983, "Remote Service Error(远端服务出错, 联系运维处理)"),
  DATASOURCE_NOT_FOUND(99988, "datasource not found(未找到数据源)"),
  PARAM_VALIDATE_FAILED(99986, "Param Validate Failed(参数校验出错)"),
  ENVID_ATYPICAL(99986, "envId atypical "),
  IS_NULL_MS(99986, ""),
  EXPRESS_IS_NULL(99987, ""),
  OPERATE_FILE_IN_REQUEST(99987, "Fail to operate file in request(上传文件处理失败)");

  /** 错误码 */
  private final int errorCode;
  /** 错误描述 */
  private final String errorDesc;

  LinkisDatasourceManagerErrorCodeSummary(int errorCode, String errorDesc) {
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
