/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.webank.wedatasphere.linkis.entrance.persistence;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.cs.CSEntranceHelper;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceExecutorManager;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecuteRequest;
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest;
import com.webank.wedatasphere.linkis.governance.common.entity.job.SubJobInfo;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager;
import com.webank.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPersistenceManager extends PersistenceManager{

    private EntranceContext entranceContext;
    private PersistenceEngine persistenceEngine;
    private ResultSetEngine resultSetEngine;
    private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceManager.class);
  //  private EntranceWebSocketService entranceWebSocketService; //TODO The latter version, to be removed, webSocket unified walk ListenerBus(后面的版本，要去掉，webSocket统一走ListenerBus)

    public void setPersistenceEngine(PersistenceEngine persistenceEngine) {
        this.persistenceEngine = persistenceEngine;
    }

    public void setResultSetEngine(ResultSetEngine resultSetEngine) {
        this.resultSetEngine = resultSetEngine;
    }

    //TODO The latter version, to be removed, webSocket unified walk ListenerBus(后面的版本，要去掉，webSocket统一走ListenerBus)
//    public void setEntranceWebSocketService(EntranceWebSocketService entranceWebSocketService) {
//        this.entranceWebSocketService = entranceWebSocketService;
//    }

    @Override
    public EntranceContext getEntranceContext() {
        return entranceContext;
    }

    @Override
    public void setEntranceContext(EntranceContext entranceContext) {
    this.entranceContext = entranceContext;
    }

    @Override
    public PersistenceEngine createPersistenceEngine() {
        return this.persistenceEngine;
    }
    @Override
    public ResultSetEngine createResultSetEngine() {
        return resultSetEngine;
    }

    @Override
    public void onResultSetCreated(EntranceExecuteRequest request, OutputExecuteResponse response) {
        String path;
        try {
            path = createResultSetEngine().persistResultSet(request, response);
        } catch (Throwable e) {
            EntranceJob job = null;
            ExecutorManager executorManager = getEntranceContext().getOrCreateScheduler().getSchedulerContext().getOrCreateExecutorManager();
            if (EntranceExecutorManager.class.isInstance(executorManager)) {
                job = (EntranceJob) ((EntranceExecutorManager) executorManager).getEntranceJobByExecId(request.getJob().getId()).getOrElse(null);
            }
            String msg = "Persist resultSet failed for subJob : " + request.getSubJobInfo().getSubJobDetail().getId() + ", response : " + BDPJettyServerHelper.gson().toJson(response);
            logger.error(msg);
            if (null != job) {
                job.onFailure("persist resultSet failed!", e);
            } else {
                logger.error("Cannot find job : {} in cache of ExecutorManager.", request.getJob().getJobRequest().getId());
            }
            return;
        }
        if(StringUtils.isNotBlank(path)) {
            EntranceJob job = null;
            try {
                ExecutorManager executorManager = getEntranceContext().getOrCreateScheduler().getSchedulerContext().getOrCreateExecutorManager();
                if (EntranceExecutorManager.class.isInstance(executorManager)) {
                    job = (EntranceJob) ((EntranceExecutorManager) executorManager).getEntranceJobByExecId(request.getJob().getId()).getOrElse(null);
                }
            } catch (Throwable e) {
                try {
                    entranceContext.getOrCreateLogManager().onLogUpdate(job, "store resultSet failed! reason: " + ExceptionUtils.getRootCauseMessage(e));
                    logger.error("store resultSet failed! reason:", e);
                } catch (Throwable e1){
                    logger.error("job {} onLogUpdate error, reason:", job.getId(), e1);
                }
                return;
            }
            SubJobInfo subJobInfo = request.getJob().getRunningSubJob();
            String resultLocation = request.getJob().getRunningSubJob().getSubJobDetail().getResultLocation();
            if (StringUtils.isEmpty(resultLocation)) {
                synchronized (subJobInfo.getSubJobDetail()) {
                    // todo check
                    if(StringUtils.isNotEmpty(subJobInfo.getSubJobDetail().getResultLocation())) {
                        return;
                    }
                    try {
                        subJobInfo.getSubJobDetail().setResultLocation(new FsPath(path).getSchemaPath());
                        createPersistenceEngine().updateIfNeeded(subJobInfo);
                    } catch (Throwable e) {
                        entranceContext.getOrCreateLogManager().onLogUpdate(job, e.toString());
                    }
                }
            }
        }
    }

    @Override
    public void onProgressUpdate(Job job, float progress, JobProgressInfo[] progressInfo) {
        float updatedProgress = progress;
        if (progress < 0) {
            logger.error("Got negitive progress : " + progress + ", job : " + ((EntranceJob) job).getJobRequest().getId());
            // todo check
            updatedProgress = -1 * progress;
        }
        job.setProgress(updatedProgress);
        EntranceJob entranceJob = (EntranceJob) job;
        entranceJob.getJobRequest().setProgress(String.valueOf(updatedProgress));
        updateJobStatus(job);
    }

    @Override
    public void onJobScheduled(Job job) {
        updateJobStatus(job);
    }
    @Override
    public void onJobInited(Job job) {
        updateJobStatus(job);
    }
    @Override
    public void onJobRunning(Job job) {
        updateJobStatus(job);
    }

    @Override
    public void onJobWaitForRetry(Job job) {
        updateJobStatus(job);
    }

    @Override
    public void onJobCompleted(Job job) {
        //update by peaceWong to set jobID to CS
        try {
            if (job.isSucceed()) {
                CSEntranceHelper.registerCSRSData(job);
            }
        } catch (Throwable e) {
            logger.error("Failed to register cs rs data ", e);
        }
        updateJobStatus(job);
    }

    private void updateJobStatus(Job job){
        JobRequest jobRequest = null;
        if(job.isCompleted()) {
            job.setProgress(1);
        }
        try{
            jobRequest = ((EntranceJob) job).getJobRequest();
            if (job.isSucceed()){
                //如果是job是成功的，那么需要将task的错误描述等都要设置为null
                jobRequest.setErrorCode(0);
                jobRequest.setErrorDesc(null);
            }
        } catch(Exception e){
            entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
            logger.error("update job status failed, reason:", e);
        }
        try {
            createPersistenceEngine().updateIfNeeded(jobRequest);
        } catch (ErrorException e) {
            entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
            logger.error("update job status failed, reason: ", e);
        }
    }

    @Override
    public void onResultSizeCreated(EntranceExecuteRequest entranceExecuteRequest, int resultSize) {
        entranceExecuteRequest.getSubJobInfo().getSubJobDetail().setResultSize(resultSize);
    }
}
