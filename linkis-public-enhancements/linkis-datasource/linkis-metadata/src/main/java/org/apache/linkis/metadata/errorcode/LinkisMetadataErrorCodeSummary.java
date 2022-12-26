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

package org.apache.linkis.metadata.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisMetadataErrorCodeSummary implements LinkisErrorCode {
  UNRECOGNIZED_IMPORT_TYPE(57895, "unrecognized import type(无法识别的导入类型)"),
  IMPORT_HIVE_SOURCE_IS_NULL(57895, "import hive source is null(导入配置单元源为空)"),
  HIVE_CREATE_IS_NULL(
      57895, "Hive create table destination database or tablename is null(hive 创建表目标数据库或表名为空)"),
  HIVE_CREATE__TABLE_IS_NULL(57895, "hive create table source table name is null(hive 创建表源表名为空)"),
  PARTITION_IS_NULL(57895, "partition name or type is null(分区名称或类型为空)"),
  EXPRESS_CODE(57895, "");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisMetadataErrorCodeSummary(int errorCode, String errorDesc) {
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
