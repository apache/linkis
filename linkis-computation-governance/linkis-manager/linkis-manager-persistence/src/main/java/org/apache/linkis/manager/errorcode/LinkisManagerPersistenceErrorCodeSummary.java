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

package org.apache.linkis.manager.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisManagerPersistenceErrorCodeSummary implements LinkisErrorCode {
  BEANUTILS_POPULATE_FAILED(10000, "beanutils populate failed (beanutils 填充失败)"),
  NODE_INSTANCE_ALREADY_EXISTS(41001, "Node instance already exists(Node实例已存在)"),
  NODE_INSTANCE_DOES_NOT_EXIST(41002, "Node instance does not exist(Node实例不存在)"),
  THE_EMNODE_IS_NULL(410002, "The emNode:{0} is null (emNode:{0} 为空)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisManagerPersistenceErrorCodeSummary(int errorCode, String errorDesc) {
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
