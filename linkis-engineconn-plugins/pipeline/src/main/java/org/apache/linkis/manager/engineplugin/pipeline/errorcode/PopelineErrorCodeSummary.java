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

package org.apache.linkis.manager.engineplugin.pipeline.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum PopelineErrorCodeSummary implements LinkisErrorCode {
  NOT_A_RESULT_SET_FILE(70001, "Not a result set file(不是结果集文件)"),
  ONLY_RESULT_CONVERTED_TO_CSV(
      70002, "Only result sets of type TABLE can be converted to CSV(只有table类型的结果集才能转为csv)"),
  ONLY_RESULT_CONVERTED_TO_EXCEL(
      70004, "Only result sets of type Table can be converted to Excel(只有table类型的结果集才能转为excel)"),
  EMPTY_DIR(70005, "empty dir!(空目录！)"),
  EXPROTING_MULTIPLE(
      70006, "Exporting multiple result sets to CSV is not supported(不支持多结果集导出为CSV)"),
  ILLEGAL_OUT_SCRIPT(70007, "Illegal out script statement(非法的out脚本语句)"),
  UNSUPPORT_OUTPUT_TYPE(70008, "unsupport output type(不支持输出类型)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  PopelineErrorCodeSummary(int errorCode, String errorDesc) {
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
