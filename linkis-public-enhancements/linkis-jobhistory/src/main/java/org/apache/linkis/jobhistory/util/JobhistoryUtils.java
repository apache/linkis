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

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.governance.common.protocol.conf.DepartmentRequest;
import org.apache.linkis.governance.common.protocol.conf.DepartmentResponse;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.entity.QueryTaskVO;
import org.apache.linkis.rpc.Sender;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobhistoryUtils {
  private static final Logger logger = LoggerFactory.getLogger(JobhistoryUtils.class);
  public static String headersStr =
      "任务ID,来源,查询语句,状态,已耗时,关键信息,是否复用,申请开始时间,申请结束时间,申请耗时,应用/任务类型/引擎,用户,创建时间";
  public static String headersEnStr =
      "JobID,Source,Execution Code,Status,Time Elapsed,Key Information,IsRuse,Application Start Time,Application End Time,Application Takes Time,App /Run Type/Engine,User,Created at";
  private static Sender sender =
      Sender.getSender(
          Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());;

  public static final String shellPath = Configuration.getLinkisHome() + "/admin/";
  public static final String analyzeFilePath = "tools/linkis-analyze.sh";

  public static byte[] downLoadJobToExcel(
      List<QueryTaskVO> jobHistoryList,
      String language,
      Boolean isAdminView,
      Boolean isDeptView,
      Long pageInfo)
      throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Workbook workbook = new XSSFWorkbook();
    byte[] byteArray = new byte[0];
    Sheet sheet = workbook.createSheet("任务信息表");
    // Create header row
    Row headerRow = sheet.createRow(0);
    String headers = "";
    Boolean viewResult = isAdminView || isDeptView;
    if (!"en".equals(language)) {
      if (viewResult) {
        headers = headersStr;
      } else {
        headers = headersStr.replace(",用户", "");
      }
    } else {
      if (viewResult) {
        headers = headersEnStr;
      } else {
        headers = headersEnStr.replace(",User", "");
      }
    }
    String[] headersArray = headers.split(",");
    for (int i = 0; i < headersArray.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headersArray[i]);
    }
    // Add data rows
    int rowNum = 1;
    List<List<QueryTaskVO>> batches = Lists.partition(jobHistoryList, 5000);
    for (List<QueryTaskVO> queryTaskList : batches) {
      for (QueryTaskVO queryTaskVO : queryTaskList) {
        Row row = sheet.createRow(rowNum++);
        createCell(row, 0, queryTaskVO.getTaskID());
        createCell(row, 1, queryTaskVO.getSourceTailor());
        createCell(row, 2, abbreviateExecutionCode(queryTaskVO.getExecutionCode()));
        createCell(row, 3, queryTaskVO.getStatus());
        if (null == queryTaskVO.getCostTime()) {
          queryTaskVO.setCostTime(0L);
        }
        createCell(row, 4, Utils.msDurationToString(queryTaskVO.getCostTime()));
        createCell(row, 5, queryTaskVO.getErrDesc());
        createCell(row, 6, formatIsReuse(queryTaskVO.getIsReuse()));
        createCell(row, 7, formatDateTime(queryTaskVO.getRequestStartTime()));
        createCell(row, 8, formatDateTime(queryTaskVO.getRequestEndTime()));
        if (null == queryTaskVO.getRequestSpendTime()) {
          queryTaskVO.setRequestSpendTime(0L);
        }
        createCell(row, 9, Utils.msDurationToString(queryTaskVO.getRequestSpendTime()));
        createCell(
            row,
            10,
            formatExecuteApplicationName(
                queryTaskVO.getExecuteApplicationName(),
                queryTaskVO.getRequestApplicationName(),
                queryTaskVO.getRunType()));
        if (viewResult) {
          createCell(row, 11, queryTaskVO.getUmUser());
          createCell(row, 12, formatDateTime(queryTaskVO.getCreatedTime()));
        } else {
          createCell(row, 11, formatDateTime(queryTaskVO.getCreatedTime()));
        }
      }
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

  public static String getDepartmentByuser(String username) {
    String departmentId = "";
    try {
      Object responseObject = sender.ask(new DepartmentRequest(username));
      if (responseObject instanceof DepartmentResponse) {
        DepartmentResponse departmentResponse = (DepartmentResponse) responseObject;
        if (StringUtils.isNotBlank(departmentResponse.departmentId())) {
          departmentId = departmentResponse.departmentId();
        }
      }
    } catch (Exception e) {
      logger.warn("get user {} departmentId  error: {}", username, e);
    }
    return departmentId;
  }
  // 创建单元格的方法
  private static void createCell(Row row, int columnIndex, Object value) {
    Cell cell = row.createCell(columnIndex);
    if (value != null) {
      cell.setCellValue(value.toString());
    }
  }

  // 截断executionCode的方法
  private static String abbreviateExecutionCode(String executionCode) {
    return StringUtils.abbreviate(executionCode, 1000);
  }

  // 格式化isReuse的方法
  private static String formatIsReuse(Boolean isReuse) {
    return isReuse != null ? (isReuse ? "是" : "否") : "";
  }

  // 格式化日期的方法
  private static String formatDateTime(Date date) {
    return date != null ? TaskConversions.dateFomat(date) : "";
  }

  // 格式化executeApplicationName的方法
  private static String formatExecuteApplicationName(
      String executeApplicationName, String requestApplicationName, String runType) {
    return requestApplicationName + "/" + runType + "/" + executeApplicationName;
  }

  public static String getDiagnosisMsg(String taskID) {
    // 执行shell 脚本获取诊断信息
    List<String> cmdlist = new ArrayList<>();
    cmdlist.add("sh");
    cmdlist.add(shellPath + analyzeFilePath);
    cmdlist.add(taskID);
    return Utils.exec(cmdlist.toArray(new String[3]), 600 * 1000L);
  }
}
