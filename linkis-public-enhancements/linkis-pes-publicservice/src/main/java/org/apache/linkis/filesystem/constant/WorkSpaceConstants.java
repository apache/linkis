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

package org.apache.linkis.filesystem.constant;

public class WorkSpaceConstants {
  public static final String XLSX_RESPONSE_CONTENT_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static final String DEFAULT_DATE_TYPE = "yyyy-MM-dd HH:mm:ss";
  public static final String LOCAL_RETURN_TYPE = "Local";
  public static final String BLANK = "BLANK";
  public static final String PARAMETER_NOT_BLANK = "Parameter {0} cannot be empty （参数不能为空 {0}）";
  public static final String FILEPATH_ILLEGALITY = "File path illegality : {0} （文件路径错误 : {0}）";
  public static final String FILE_PERMISSION_ERROR =
      "File permissions prohibit modification of unreadable files: {0}  （文件权限禁止修改不可读: {0}）";
  public static final String FILEPATH_ILLEGAL_SYMBOLS =
      "File path illegal symbols : {0} （文件路径结果集路径包含非法字符 : {0}）";

  public static final String HIVE_FILEPATH_ILLEGAL_SYMBOLS =
      "Hive file HDFS path is illegal: {0}, please use/live/workplace/related path （Hive文件hdfs路径非法: {0},请使用/hive/warehouse/相关路径并指定到表级别）";
}
