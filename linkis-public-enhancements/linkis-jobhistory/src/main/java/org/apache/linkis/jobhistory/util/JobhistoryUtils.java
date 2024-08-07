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

package org.apache.linkis.jobhistory.util;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.entity.QueryTaskVO;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class JobhistoryUtils {

  public static String headersStr =
      "任务ID,来源,查询语句,状态,已耗时,关键信息,应用/引擎,创建时间,是否复用,申请开始时间,申请结束时间,申请花费时间";
  public static String headersEnStr =
      "JobID,Source,Execution Code,Status,Time Elapsed,Key Information,App / Engine,Created at,IsRuse,Application Start Time,Application End Time,Application Takes Time";

  public static byte[] downLoadJobToExcel(List<QueryTaskVO> jobHistoryList, String language)
      throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Workbook workbook = new XSSFWorkbook();
    byte[] byteArray = new byte[0];

    Sheet sheet = workbook.createSheet("任务信息表");
    // Create header row
    Row headerRow = sheet.createRow(0);
    String headers = "";
    if (!"en".equals(language)) {
      headers = headersStr;
    } else {
      headers = headersEnStr;
    }
    String[] headersArray = headers.split(",");
    for (int i = 0; i < headersArray.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headersArray[i]);
    }
    // Add data rows
    int rowNum = 1;
    for (QueryTaskVO queryTaskVO : jobHistoryList) {
      Row row = sheet.createRow(rowNum++);
      row.createCell(0).setCellValue(queryTaskVO.getTaskID());
      row.createCell(1).setCellValue(queryTaskVO.getSourceTailor());
      row.createCell(2).setCellValue(queryTaskVO.getExecutionCode());
      row.createCell(3).setCellValue(queryTaskVO.getStatus());
      if (null == queryTaskVO.getCostTime()) {
        queryTaskVO.setCostTime(0L);
      }
      row.createCell(4).setCellValue(Utils.msDurationToString(queryTaskVO.getCostTime()));
      row.createCell(5).setCellValue(queryTaskVO.getErrDesc());
      row.createCell(6)
          .setCellValue(
              queryTaskVO.getExecuteApplicationName()
                  + "/"
                  + queryTaskVO.getRequestApplicationName());
      row.createCell(7).setCellValue(TaskConversions.dateFomat(queryTaskVO.getCreatedTime()));
      row.createCell(8).setCellValue(queryTaskVO.getIsReuse());
      row.createCell(9).setCellValue(TaskConversions.dateFomat(queryTaskVO.getRequestStartTime()));
      row.createCell(10).setCellValue(TaskConversions.dateFomat(queryTaskVO.getRequestEndTime()));
      if (null == queryTaskVO.getRequestSpendTime()) {
        queryTaskVO.setRequestSpendTime(0L);
      }
      row.createCell(11).setCellValue(Utils.msDurationToString(queryTaskVO.getRequestSpendTime()));
    }
    try {
      workbook.write(outputStream);
      byteArray = outputStream.toByteArray();
      workbook.close();
    } catch (IOException e) {
      throw new IOException(e);
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
    return byteArray;
  }
}
