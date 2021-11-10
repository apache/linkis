/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cli.application.driver.transformer;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.constants.UjesClientDriverConstants;
import org.apache.linkis.cli.application.driver.UjesClientDriver;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobInfo;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobKill;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobSubmitExec;
import org.apache.linkis.cli.application.presenter.model.LinkisJobIncLogModel;
import org.apache.linkis.cli.application.presenter.model.LinkisJobResultModel;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobExec;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.TransformerException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;
import org.apache.linkis.cli.core.presenter.model.JobExecModel;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.linkis.ujes.client.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description: convert data in order to fit response of {@link UjesClientDriver} into a form that acceptable by linkis-cli
 */
public class UjesClientDriverTransformer implements DriverTransformer {
    private Logger logger = LoggerFactory.getLogger(UjesClientDriverTransformer.class);


    public String convertEngineType(CmdType cmdType) {
        if (StringUtils.equalsIgnoreCase(cmdType.getName(), "spark")) {
            return UjesClientDriverConstants.DEFAULT_SPARK_ENGINE;
        }
        if (StringUtils.equalsIgnoreCase(cmdType.getName(), "hive")) {
            return UjesClientDriverConstants.DEFAULT_HIVE_ENGINE;
        } else {
            throw new TransformerException("TFM0001", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert engine type: \"" + cmdType.getName() + "\" is not supported");
        }
    }

    @Override
    public String convertJobID(String taskID) {
        return AppConstants.JOB_ID_PREFIX + taskID;
    }

    @Override
    public JobStatus convertJobStatus(String jobStatus) {
        if (StringUtils.isNotBlank(jobStatus)) {
            if (JobStatus.INITED.name().equalsIgnoreCase(jobStatus)) return JobStatus.INITED;
            else if (JobStatus.WAIT_FOR_RETRY.name().equalsIgnoreCase(jobStatus)) return JobStatus.WAIT_FOR_RETRY;
            else if (JobStatus.SCHEDULED.name().equalsIgnoreCase(jobStatus)) return JobStatus.SCHEDULED;
            else if (JobStatus.RUNNING.name().equalsIgnoreCase(jobStatus)) return JobStatus.RUNNING;
            else if (JobStatus.SUCCEED.name().equalsIgnoreCase(jobStatus)) return JobStatus.SUCCEED;
            else if (JobStatus.FAILED.name().equalsIgnoreCase(jobStatus)) return JobStatus.FAILED;
            else if (JobStatus.CANCELLED.name().equalsIgnoreCase(jobStatus)) return JobStatus.CANCELLED;
            else if (JobStatus.TIMEOUT.name().equalsIgnoreCase(jobStatus)) return JobStatus.TIMEOUT;
            else return JobStatus.UNKNOWN;
        } else {
            return JobStatus.UNKNOWN;
        }
    }

    @Override
    public JobExec convertAndUpdateExecData(JobExec execData, DWSResult result) {
        if (result == null) {
            return execData;
        }
        if (!(execData instanceof LinkisJobSubmitExec) &&
                !(execData instanceof LinkisJobInfo) &&
                !(execData instanceof LinkisJobKill)) {
            throw new TransformerException("TFM0013", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert DWSResult into JobSubmitExec: execData is of wrong data type");

        }
        if (execData instanceof LinkisJobSubmitExec) {
            LinkisJobSubmitExec linkisExecData = (LinkisJobSubmitExec) execData;
            if (result instanceof JobInfoResult) {
                execData = updateSubmitExecByJobInfoResult(linkisExecData, (JobInfoResult) result);
            } else if (result instanceof JobSubmitResult) {
                execData = updateExecDataByJobSubmitResult(linkisExecData, (JobSubmitResult) result);
            } else {
                throw new TransformerException("TFM0002", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                        "Failed to convert DWSResult into JobSubmitExec: \"" + result.getClass().getCanonicalName() + "\" is not supported");
            }
        } else if (execData instanceof LinkisJobInfo) {
            LinkisJobInfo linkisJobInfoData = (LinkisJobInfo) execData;
            if (result instanceof JobInfoResult) {
                execData = updateInfoDataByJobInfoResult(linkisJobInfoData, (JobInfoResult) result);
            } else {
                throw new TransformerException("TFM0002", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                        "Failed to convert DWSResult into JobSubmitExec: \"" + result.getClass().getCanonicalName() + "\" is not supported");
            }
        } else if (execData instanceof LinkisJobKill) {
            LinkisJobKill killData = (LinkisJobKill) execData;
            if (result instanceof JobInfoResult) {
                execData = updateKillDataByJobInfoResult(killData, (JobInfoResult) result);
            } else {
                throw new TransformerException("TFM0002", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                        "Failed to convert DWSResult into JobSubmitExec: \"" + result.getClass().getCanonicalName() + "\" is not supported");
            }
        }
        return execData;
    }

    @Override
    public JobExecModel convertAndUpdateModel(JobExecModel model, DWSResult result) {
        if (result == null) {
            return model;
        }
        if (result instanceof JobLogResult && model instanceof LinkisJobIncLogModel) {
            model = updateIncLogModelByJobLogResult((LinkisJobIncLogModel) model, (JobLogResult) result);
        } else if (result instanceof JobInfoResult && model instanceof LinkisJobIncLogModel) {
            model = updateModelByJobInfo((LinkisJobIncLogModel) model, (JobInfoResult) result);
        } else if (result instanceof OpenLogResult && model instanceof LinkisJobIncLogModel) {
            model = updateIncLogModelByOpenLogResult((LinkisJobIncLogModel) model, (OpenLogResult) result);
        } else if (result instanceof ResultSetResult && model instanceof LinkisJobResultModel) {
            model = updateModelByResultSet((LinkisJobResultModel) model, (ResultSetResult) result);
        } else {
            throw new TransformerException("TFM0003", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert DWSResult: \"" + result.getClass().getCanonicalName() + "\" into Model: \"" + model.getClass().getCanonicalName() + "\": conversion is not supported");
        }
        return model;
    }


    @Override
    public List<LinkedHashMap<String, String>> convertResultMeta(Object rawMetaData) {
        if (rawMetaData == null) {
            throw new TransformerException("TFM0004", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert ResultSetMeta: meta data is null");
        }
        if (rawMetaData == null ||
                (rawMetaData instanceof String &&
                        (StringUtils.equalsIgnoreCase((String) rawMetaData, "NULL")))) {
            return null;
        }
        List<LinkedHashMap<String, String>> ret;

        try {
            ret = (List<LinkedHashMap<String, String>>) rawMetaData;
        } catch (Exception e) {
            throw new TransformerException("TFM0005", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert ResultSetMeta", e);
        }
        return ret;
    }

    @Override
    public List<List<String>> convertResultContent(Object rawContent) {
        if (rawContent == null) {
            return null;
        }
        List<List<String>> ret;

        try {
            ret = (List<List<String>>) rawContent;
        } catch (Exception e) {
            throw new TransformerException("TFM0007", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert ResultSet", e);
        }
        return ret;
    }

    private JobSubmitExec updateExecDataByJobSubmitResult(LinkisJobSubmitExec execData, JobSubmitResult jobExecuteResult) {

        if (jobExecuteResult != null &&
                jobExecuteResult.getTaskID() != null) {
            execData.setTaskID(jobExecuteResult.getTaskID());

            if (0 == jobExecuteResult.getStatus()) {
                execData.setJobStatus(JobStatus.SUBMITTING);
            } else {
                execData.setJobStatus(JobStatus.FAILED);
            }
            execData.setJobID(this.convertJobID(jobExecuteResult.getTaskID()));
            execData.setUser(jobExecuteResult.getUser());

            if (Utils.isValidExecId(jobExecuteResult.getExecID())) {
                execData.setExecID(jobExecuteResult.getExecID());
            }
        } else {
            execData.setJobStatus(JobStatus.FAILED);
        }
        return execData;

    }


    private LinkisJobSubmitExec updateSubmitExecByJobInfoResult(LinkisJobSubmitExec execData, JobInfoResult result) {

        if (execData.getTaskID() == null) {
            if (result != null &&
                    result.getRequestPersistTask() != null &&
                    result.getRequestPersistTask().getTaskID() != null) {
                String candidateTaskID = String.valueOf(result.getRequestPersistTask().getTaskID());
                if (StringUtils.isNotBlank(candidateTaskID)) {
                    execData.setTaskID(candidateTaskID);
                }
            }
        }

        if (!Utils.isValidExecId(execData.getExecID())) {
            String execId = null;
            if (result != null &&
                    result.getTask() != null &&
                    result.getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
                execId = (String) result.getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
            }
            if (Utils.isValidExecId(execId)) {
                execData.setExecID(execId);
            }

        }

        if (result != null &&
                StringUtils.isNotBlank(result.getUser())) {
            if (StringUtils.isBlank(execData.getUser())) {
                execData.setUser(result.getUser());
            }
        }

        if (result != null && result.getJobStatus() != null) {
            JobStatus jobStatus = this.convertJobStatus(result.getJobStatus());

            execData.setJobStatus(jobStatus);
            execData.setJobProgress(result.getRequestPersistTask().getProgress());
        }
        if (result != null &&
                result.getRequestPersistTask() != null &&
                result.getRequestPersistTask().getLogPath() != null) {
            String logPath = result.getRequestPersistTask().getLogPath();
            if (StringUtils.isNotBlank(logPath)) {
                execData.setLogPath(logPath);
            }
        }
        if (result != null &&
                result.getRequestPersistTask() != null &&
                result.getRequestPersistTask().getResultLocation() != null &&
                StringUtils.isNotBlank(result.getRequestPersistTask().getResultLocation())) {
            String resultLocation = result.getRequestPersistTask().getResultLocation();
            if (StringUtils.isNotBlank(resultLocation)) {
                execData.setResultLocation(resultLocation);
            }
        }

        if (result != null &&
                result.getTask() != null &&
                result.getTask().get(LinkisKeys.KEY_ERROR_DESC) != null &&
                StringUtils.isNotBlank((String) result.getTask().get(LinkisKeys.KEY_ERROR_DESC))) {
            Integer errCode = (Integer) result.getTask().get(LinkisKeys.KEY_ERROR_CODE);
            String errDesc = (String) result.getTask().get(LinkisKeys.KEY_ERROR_DESC);
            execData.setErrCode(errCode);
            execData.setErrDesc(errDesc);
        }

        return execData;
    }

    private LinkisJobInfo updateInfoDataByJobInfoResult(LinkisJobInfo execData, JobInfoResult result) {
        if (result != null &&
                result.getRequestPersistTask() != null &&
                result.getRequestPersistTask().getTaskID() != null) {
            String candidateTaskID = String.valueOf(result.getRequestPersistTask().getTaskID());
            if (StringUtils.isNotBlank(candidateTaskID)) {
                execData.setTaskID(candidateTaskID);
            }
        }

        String strongerExecId = null;
        if (result != null &&
                result.getTask() != null &&
                result.getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
            strongerExecId = (String) result.getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
        }
        execData.setStrongerExecId(strongerExecId);

        if (result != null && result.getRequestPersistTask() != null) {
            execData.setExecId(result.getRequestPersistTask().getExecId());
            execData.setRunType(result.getRequestPersistTask().getRunType());
            execData.setCreatedTime(result.getRequestPersistTask().getCreatedTime());
            execData.setUpdatedTime(result.getRequestPersistTask().getUpdatedTime());
            execData.setErrCode(result.getRequestPersistTask().getErrCode());
            execData.setErrMsg(result.getRequestPersistTask().getErrDesc());
            execData.setEngineStartTime(result.getRequestPersistTask().getEngineStartTime());
            execData.setInstance((result.getRequestPersistTask().getInstance()));
            execData.setProgress(result.getRequestPersistTask().getProgress());
            execData.setLogPath(result.getRequestPersistTask().getLogPath());
            execData.setUmUser(result.getRequestPersistTask().getUmUser());
            execData.setRequestApplicationName(result.getRequestPersistTask().getRequestApplicationName());
            execData.setExecuteApplicationName(result.getRequestPersistTask().getExecuteApplicationName());
        }
        if (result != null &&
                result.getTask() != null) {
            execData.setEngineType((String) result.getTask().get("engineType"));
            execData.setCostTime((Integer) result.getTask().get("costTime"));
        }

        if (result != null && result.getJobStatus() != null) {
            JobStatus jobStatus = this.convertJobStatus(result.getJobStatus());
            execData.setJobStatus(jobStatus);
        }


        return execData;
    }

    private LinkisJobKill updateKillDataByJobInfoResult(LinkisJobKill killData, JobInfoResult result) {
        if (killData.getTaskID() == null) {
            if (result != null &&
                    result.getRequestPersistTask() != null &&
                    result.getRequestPersistTask().getTaskID() != null) {
                String candidateTaskID = String.valueOf(result.getRequestPersistTask().getTaskID());
                if (StringUtils.isNotBlank(candidateTaskID)) {
                    killData.setTaskID(candidateTaskID);
                }
            }
        }

        String strongerExecId = null;
        if (result != null &&
                result.getTask() != null &&
                result.getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
            strongerExecId = (String) result.getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
        }
        killData.setExecID(strongerExecId);

        if (result != null &&
                StringUtils.isNotBlank(result.getUser())) {
            if (StringUtils.isBlank(killData.getUser())) {
                killData.setUser(result.getUser());
            }
        }

        if (result != null && result.getJobStatus() != null) {
            JobStatus jobStatus = this.convertJobStatus(result.getJobStatus());
            killData.setJobStatus(jobStatus);
        }

        return killData;
    }


    private LinkisJobIncLogModel updateIncLogModelByJobLogResult(LinkisJobIncLogModel model, JobLogResult jobLogResult) {

        if (jobLogResult != null &&
                jobLogResult.getLog() != null &&
                StringUtils.isNotBlank(jobLogResult.getLog().get(UjesClientDriverConstants.IDX_FOR_LOG_TYPE_ALL))) {
            model.setFromLine(jobLogResult.getFromLine());
            String incLog = jobLogResult.getLog().get(UjesClientDriverConstants.IDX_FOR_LOG_TYPE_ALL);
            model.writeIncLog(incLog);
        }
        return model;
    }

    private LinkisJobIncLogModel updateIncLogModelByOpenLogResult(LinkisJobIncLogModel model, OpenLogResult openLogResult) {
        if (openLogResult != null &&
                openLogResult.getLog() != null &&
                StringUtils.isNotBlank(openLogResult.getLog()[UjesClientDriverConstants.IDX_FOR_LOG_TYPE_ALL])) {
            String incLog = openLogResult.getLog()[UjesClientDriverConstants.IDX_FOR_LOG_TYPE_ALL];
            model.writeIncLog(incLog);
        }
        return model;
    }

    private LinkisJobResultModel updateModelByResultSet(LinkisJobResultModel model, ResultSetResult resultSetResult) {
        LinkisJobResultModel resultModel = (LinkisJobResultModel) model;
        if (resultSetResult != null) {
            if (resultSetResult.getMetadata() != null) {
                resultModel.setResultMetaData(resultSetResult.getMetadata());
            }
            resultModel.setResultContent(resultSetResult.getFileContent()); // can be null if reaches non-existing page
            if (resultSetResult.getTotalPage() > 0) {
                resultModel.setTotalPage(resultSetResult.getTotalPage());
            }
        }
        return resultModel;
    }


    private LinkisJobIncLogModel updateModelByJobInfo(LinkisJobIncLogModel model, JobInfoResult result) {
        if (model.getTaskID() == null) {
            if (result != null &&
                    result.getRequestPersistTask() != null &&
                    result.getRequestPersistTask().getTaskID() != null) {
                String candidateTaskID = String.valueOf(result.getRequestPersistTask().getTaskID());
                if (StringUtils.isNotBlank(candidateTaskID)) {
                    model.setTaskID(candidateTaskID);
                }
            }
        }

        if (!Utils.isValidExecId(model.getExecID())) {
            String execId = null;
            if (result != null &&
                    result.getTask() != null &&
                    result.getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
                execId = (String) result.getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
            }
            if (result != null &&
                    result.getTask() != null &&
                    result.getTask().containsKey(LinkisKeys.KEY_STRONGER_EXECID)) {
                execId = (String) result.getTask().get(LinkisKeys.KEY_STRONGER_EXECID);
            }
            if (Utils.isValidExecId(execId)) {
                model.setExecID(execId);
            }

        }

        if (result != null && result.getJobStatus() != null) {
            JobStatus jobStatus = this.convertJobStatus(result.getJobStatus());

            model.setJobStatus(jobStatus);
            model.setJobProgress(result.getRequestPersistTask().getProgress());
        }
        if (result != null &&
                result.getRequestPersistTask() != null &&
                result.getRequestPersistTask().getLogPath() != null) {
            String logPath = result.getRequestPersistTask().getLogPath();
            if (StringUtils.isNotBlank(logPath)) {
                model.setLogPath(logPath);
            }
        }

        return model;
    }

}