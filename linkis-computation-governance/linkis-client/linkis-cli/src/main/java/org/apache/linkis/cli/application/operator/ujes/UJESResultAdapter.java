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

package org.apache.linkis.cli.application.operator.ujes;

import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.exception.TransformerException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.common.LinkisJobStatus;
import org.apache.linkis.cli.application.interactor.job.common.ResultSet;
import org.apache.linkis.cli.application.operator.ujes.result.OpenLogResult2;
import org.apache.linkis.cli.application.operator.ujes.result.ResultSetResult2;
import org.apache.linkis.cli.application.operator.ujes.result.UJESResult;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.linkis.ujes.client.request.UserAction;
import org.apache.linkis.ujes.client.response.JobInfoResult;
import org.apache.linkis.ujes.client.response.JobLogResult;
import org.apache.linkis.ujes.client.response.JobStatusResult;
import org.apache.linkis.ujes.client.response.JobSubmitResult;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class UJESResultAdapter implements LinkisOperResultAdapter {
  private Object result;
  private String[] resultsetArray;

  public UJESResultAdapter(Object result) {
    if (!(result instanceof DWSResult) && !(result instanceof UJESResult)) {
      throw new TransformerException(
          "TFM0001",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Input of UJESResultAdapter is not of correct type. Current type:"
              + result.getClass().getCanonicalName());
    }
    this.result = result;
  }

  public UJESResultAdapter(String[] resultsetArray) {
    this.resultsetArray = resultsetArray;
  }

  @Override
  public String getJobID() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobSubmitResult) {
      return ((JobSubmitResult) result).getTaskID();
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getRequestPersistTask() != null
          && ((JobInfoResult) result).getRequestPersistTask().getTaskID() != null) {
        return String.valueOf(((JobInfoResult) result).getRequestPersistTask().getTaskID());
      }
    }
    return null;
  }

  @Override
  public String getUser() {
    if (result == null) {
      return null;
    }
    if (result instanceof UserAction) {
      return ((UserAction) result).getUser();
    }
    return null;
  }

  @Override
  public String getProxyUser() {
    String user = null;
    String umUser = null;
    String requestApplication = null;
    String parsedUser1 = null;
    String parsedUser2 = null;
    if (result instanceof JobInfoResult) {
      if (result != null
          && ((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().containsKey(LinkisKeys.KEY_UMUSER)) {
        umUser = (String) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_UMUSER);
      }
      if (result != null
          && ((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().containsKey(LinkisKeys.KEY_REQUESTAPP)) {
        requestApplication =
            parseUserOutOfExecId(
                (String) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_REQUESTAPP),
                requestApplication);
      }
      if (result != null
          && ((JobInfoResult) result).getData() != null
          && ((JobInfoResult) result).getData().containsKey(LinkisKeys.KEY_EXECID)) {
        parsedUser1 =
            parseUserOutOfExecId(
                (String) ((JobInfoResult) result).getData().get(LinkisKeys.KEY_EXECID),
                requestApplication);
      }
      if (result != null
          && ((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
        parsedUser2 =
            parseUserOutOfStrongerExecId(
                (String) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_STRONGER_EXECID),
                requestApplication);
      }
      if (StringUtils.isNotBlank(parsedUser2)) {
        user = parsedUser2;
      } else if (StringUtils.isNotBlank(parsedUser1)) {
        user = parsedUser1;
      } else if (StringUtils.isNotBlank(umUser)) {
        user = umUser;
      }
    }
    return user;
  }

  @Override
  public JobStatus getJobStatus() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (result != null && StringUtils.isNotBlank(((JobInfoResult) result).getJobStatus())) {
        JobStatus jobStatus = convertStatusFromString(((JobInfoResult) result).getJobStatus());
        return jobStatus;
      }
    } else if (result instanceof JobStatusResult) {
      if (result != null && StringUtils.isNotBlank(((JobInfoResult) result).getJobStatus())) {
        JobStatus jobStatus = convertStatusFromString(((JobStatusResult) result).getJobStatus());
        return jobStatus;
      }
    }
    return null;
  }

  @Override
  public String getStrongerExecId() {
    if (result == null) {
      return null;
    }
    String execId = null;
    if (result instanceof JobInfoResult) {
      if (result != null
          && ((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
        execId = (String) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
      }
    }
    if (CliUtils.isValidExecId(execId)) {
      return execId;
    }
    return null;
  }

  @Override
  public Float getJobProgress() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getRequestPersistTask() != null
          && ((JobInfoResult) result).getRequestPersistTask() != null) {
        return ((JobInfoResult) result).getRequestPersistTask().getProgress();
      }
    }
    return null;
  }

  @Override
  public String getLogPath() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getRequestPersistTask() != null
          && StringUtils.isNotBlank(
              ((JobInfoResult) result).getRequestPersistTask().getLogPath())) {
        String logPath = ((JobInfoResult) result).getRequestPersistTask().getLogPath();
        return logPath;
      }
    }
    return null;
  }

  @Override
  public String getResultLocation() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getRequestPersistTask() != null
          && ((JobInfoResult) result).getRequestPersistTask().getResultLocation() != null
          && StringUtils.isNotBlank(
              ((JobInfoResult) result).getRequestPersistTask().getResultLocation())) {
        String resultLocation =
            ((JobInfoResult) result).getRequestPersistTask().getResultLocation();
        return resultLocation;
      }
    }
    return null;
  }

  @Override
  public String[] getResultSetPaths() {
    return resultsetArray;
  }

  @Override
  public Integer getErrCode() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_CODE) != null
          && ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_CODE) instanceof Integer) {
        Integer errCode =
            (Integer) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_CODE);
        return errCode;
      }
    }
    return null;
  }

  @Override
  public String getErrDesc() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult) {
      if (((JobInfoResult) result).getTask() != null
          && ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_DESC) != null
          && ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_DESC) instanceof String) {
        String errDesc = (String) ((JobInfoResult) result).getTask().get(LinkisKeys.KEY_ERROR_DESC);
        return errDesc;
      }
    }
    return null;
  }

  @Override
  public String getLog() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobLogResult && ((JobLogResult) result).getLog() != null) {
      return ((JobLogResult) result).getLog().get(UJESConstants.IDX_FOR_LOG_TYPE_ALL);
    }
    if (result instanceof OpenLogResult2
        && ((OpenLogResult2) result).getResult() != null
        && ((OpenLogResult2) result).getResult().getLog() != null) {
      String allLog =
          ((OpenLogResult2) result).getResult().getLog()[UJESConstants.IDX_FOR_LOG_TYPE_ALL];
      Integer fromLine = ((OpenLogResult2) result).getFromLine();
      return StringUtils.substring(
          allLog, getFirstIndexSkippingLines(allLog, fromLine == null ? 0 : fromLine));
    }
    return null;
  }

  @Override
  public Integer getNextLogLine() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobLogResult) {
      return ((JobLogResult) result).getFromLine();
    }
    if (result instanceof OpenLogResult2
        && ((OpenLogResult2) result).getResult() != null
        && ((OpenLogResult2) result).getResult().getLog() != null) {
      return getNumOfLines(
          ((OpenLogResult2) result).getResult().getLog()[UJESConstants.IDX_FOR_LOG_TYPE_ALL]);
    }
    return null;
  }

  @Override
  public Boolean hasNextLogLine() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobLogResult) {
      return true;
    }
    if (result instanceof OpenLogResult2) {
      return false;
    }
    return null;
  }

  @Override
  public ResultSet getResultContent() {
    if (result == null) {
      return null;
    }
    if (result instanceof ResultSetResult2
        && ((ResultSetResult2) result).getResultSetResult() != null
        && ((ResultSetResult2) result).getResultSetResult().getFileContent() != null
        && ((ResultSetResult2) result).getResultSetResult().getMetadata() != null) {
      ResultSet ret = new ResultSet();
      ret.setResultsetIdx(((ResultSetResult2) result).getIdxResultSet());
      if (((ResultSetResult2) result).getResultSetResult().getMetadata() != null) {
        ret.setResultMeta(
            this.convertResultMeta(((ResultSetResult2) result).getResultSetResult().getMetadata()));
      }
      if (((ResultSetResult2) result).getResultSetResult().getFileContent() != null) {
        ret.setContent(
            this.convertRawResultContent(
                ((ResultSetResult2) result).getResultSetResult().getFileContent()));
      }
      // can be null if reaches non-existing page
      return ret;
    }
    return null;
  }

  @Override
  public Boolean resultHasNextPage() {
    if (result == null) {
      return null;
    }
    if (result instanceof ResultSetResult2) {
      if (((ResultSetResult2) result).getResultSetResult() == null
          || ((ResultSetResult2) result).getResultSetResult().getFileContent() == null
          || (((ResultSetResult2) result).getResultSetResult().getFileContent() instanceof List
              && ((List<?>) ((ResultSetResult2) result).getResultSetResult().getFileContent())
                      .size()
                  == 0)
          || ((ResultSetResult2) result).getResultSetResult().getTotalLine() == 0) {
        return false;
      } else {
        return true;
      }
    }
    return null;
  }

  private String parseUserOutOfStrongerExecId(String strongerExecId, String requestApp) {
    int idx = StringUtils.indexOf(strongerExecId, requestApp) + StringUtils.length(requestApp) + 1;
    int idx2 = StringUtils.indexOf(strongerExecId, '_', idx);
    return StringUtils.substring(strongerExecId, idx, idx2);
  }

  private String parseUserOutOfExecId(String strongerExecId, String requestApp) {
    int idx = StringUtils.indexOf(strongerExecId, requestApp) + StringUtils.length(requestApp) + 1;
    int idx2 = StringUtils.indexOf(strongerExecId, '_', idx);
    return StringUtils.substring(strongerExecId, idx, idx2);
  }

  private JobStatus convertStatusFromString(String status) {
    return LinkisJobStatus.convertFromJobStatusString(status);
  }

  private List<LinkedHashMap<String, String>> convertResultMeta(Object rawMetaData) {
    if (rawMetaData == null) {
      return null;
    }
    if (rawMetaData instanceof String
        && StringUtils.equalsIgnoreCase((String) rawMetaData, "NULL")) {
      return null;
    }
    List<LinkedHashMap<String, String>> ret;

    try {
      ret = (List<LinkedHashMap<String, String>>) rawMetaData;
    } catch (Exception e) {
      throw new TransformerException(
          "TFM0005",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to convert ResultSetMeta",
          e);
    }
    return ret;
  }

  private List<List<String>> convertRawResultContent(Object rawContent) {
    if (rawContent == null) {
      return null;
    }
    List<List<String>> ret;

    try {
      ret = (List<List<String>>) rawContent;
    } catch (ClassCastException e) {
      throw new TransformerException(
          "TFM0007",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to convert ResultSet",
          e);
    }
    return ret;
  }

  @Override
  public String getInstance() {
    if (result == null) {
      return null;
    }
    return null;
  }

  @Override
  public String getUmUser() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getUmUser();
    }
    return null;
  }

  @Override
  public String getSimpleExecId() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getExecId();
    }
    return null;
  }

  @Override
  public String getExecutionCode() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getExecutionCode();
    }
    return null;
  }

  @Override
  public String getEngineType() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getTask() != null
        && ((JobInfoResult) result).getTask().get("engineType") != null) {
      return (String) ((JobInfoResult) result).getTask().get("engineType");
    }
    return null;
  }

  @Override
  public String getRunType() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getRunType();
    }
    return null;
  }

  @Override
  public Long getCostTime() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getTask() != null
        && ((JobInfoResult) result).getTask().get("costTime") != null) {
      return Long.valueOf((Integer) ((JobInfoResult) result).getTask().get("costTime"));
    }
    return null;
  }

  @Override
  public Date getCreatedTime() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getCreatedTime();
    }
    return null;
  }

  @Override
  public Date getUpdatedTime() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getUpdatedTime();
    }
    return null;
  }

  @Override
  public Date getEngineStartTime() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getEngineStartTime();
    }
    return null;
  }

  @Override
  public String getExecuteApplicationName() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getExecuteApplicationName();
    }
    return null;
  }

  @Override
  public String getRequestApplicationName() {
    if (result == null) {
      return null;
    }
    if (result instanceof JobInfoResult
        && ((JobInfoResult) result).getRequestPersistTask() != null) {
      return ((JobInfoResult) result).getRequestPersistTask().getRequestApplicationName();
    }
    return null;
  }

  private int getNumOfLines(String str) {
    if (str == null || str.length() == 0) {
      return 0;
    }
    int lines = 1;
    int len = str.length();
    for (int pos = 0; pos < len; pos++) {
      char c = str.charAt(pos);
      if (c == '\r') {
        lines++;
        if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
          pos++;
        }
      } else if (c == '\n') {
        lines++;
      }
    }
    return lines;
  }

  private int getFirstIndexSkippingLines(String str, Integer lines) {
    if (str == null || str.length() == 0 || lines < 0) {
      return -1;
    }
    if (lines == 0) {
      return 0;
    }

    int curLineIdx = 0;
    int len = str.length();
    for (int pos = 0; pos < len; pos++) {
      char c = str.charAt(pos);
      if (c == '\r') {
        curLineIdx++;
        if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
          pos++;
        }
      } else if (c == '\n') {
        curLineIdx++;
      } else {
        continue;
      }

      if (curLineIdx >= lines) {
        return pos + 1;
      }
    }
    return -1;
  }
}
