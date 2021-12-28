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
 
package org.apache.linkis.entrance.persistence;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.cs.CSEntranceHelper;
import org.apache.linkis.entrance.execute.EntranceExecutorManager;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.job.EntranceExecuteRequest;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.entity.job.SubJobInfo;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.scheduler.executer.ExecutorManager;
import org.apache.linkis.scheduler.executer.OutputExecuteResponse;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.server.BDPJettyServerHelper;
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
    public void onResultSetCreated(Job job, OutputExecuteResponse response) {
        String path;
        try {
            path = createResultSetEngine().persistResultSet(job, response);
        } catch (Throwable e) {
            String msg = "Persist resultSet failed for subJob : " + job.getId() + ", response : " + BDPJettyServerHelper.gson().toJson(response);
            logger.error(msg);
            if (null != job) {
                job.onFailure("persist resultSet failed!", e);
            } else {
                logger.error("Cannot find job : {} in cache of ExecutorManager.", job.getId());
            }
            return;
        }
        if(StringUtils.isNotBlank(path) && job instanceof EntranceJob) {
            EntranceJob entranceJob = (EntranceJob) job;
            SubJobInfo subJobInfo = entranceJob.getRunningSubJob();
            String resultLocation = entranceJob.getRunningSubJob().getSubJobDetail().getResultLocation();
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
        if(job.getProgress() >= 0 && job.getProgress() == updatedProgress){
            return ;
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
        //to set jobID to CS
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
            jobRequest = this.entranceContext.getOrCreateEntranceParser().parseToJobRequest(job);
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
    public void onResultSizeCreated(Job job, int resultSize) {
    }
}
