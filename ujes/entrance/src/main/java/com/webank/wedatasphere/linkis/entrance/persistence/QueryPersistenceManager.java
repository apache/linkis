/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * author: enjoyyin
 * date: 2018/9/29
 * time: 19:52
 * Description:
 */
package com.webank.wedatasphere.linkis.entrance.persistence;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.cs.CSEntranceHelper;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask;
import com.webank.wedatasphere.linkis.protocol.task.Task;
import com.webank.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
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
        boolean isEntranceJob = job instanceof EntranceJob;
        try {
            path = createResultSetEngine().persistResultSet(job, response);
        } catch (Throwable e) {
            job.onFailure("persist resultSet failed!", e);
            if(isEntranceJob) ((EntranceJob)job).incrementResultSetPersisted();
            return;
        }
        if(StringUtils.isNotBlank(path)) {
            Task task = null;
            try {
                task = this.entranceContext.getOrCreateEntranceParser().parseToTask(job);
            } catch (Throwable e) {
                try {
                    entranceContext.getOrCreateLogManager().onLogUpdate(job, "store resultSet failed! reason: " + ExceptionUtils.getRootCauseMessage(e));
                    logger.error("store resultSet failed! reason:", e);
                } catch (Throwable e1){
                    logger.error("job {} onLogUpdate error, reason:", job.getId(), e1);
                } //ignore it
                if(isEntranceJob) {
                    ((EntranceJob)job).incrementResultSetPersisted();
                }
                return;
            }
            if(task instanceof RequestPersistTask) {
                RequestPersistTask requestPersistTask = (RequestPersistTask) task;
                if(StringUtils.isEmpty(requestPersistTask.getResultLocation())) synchronized (task) {
                    if(StringUtils.isNotEmpty(requestPersistTask.getResultLocation())) {
                        if(isEntranceJob) {
                            ((EntranceJob)job).incrementResultSetPersisted();
                        }
                        return;
                    }
                    try {
                        requestPersistTask.setResultLocation(new FsPath(path).getParent().getSchemaPath());
                        createPersistenceEngine().updateIfNeeded(task);
                    } catch (Throwable e) {
                        entranceContext.getOrCreateLogManager().onLogUpdate(job, e.toString());
                    }
                }
            }
        }
        if(isEntranceJob) {
            ((EntranceJob)job).incrementResultSetPersisted();
        }
    }

    @Override
    public void onResultSizeCreated(Job job, int resultSize) {
        if(job instanceof EntranceJob) {
            ((EntranceJob) job).setResultSize(resultSize);
        }
    }

    @Override
    public void onProgressUpdate(Job job, float progress, JobProgressInfo[] progressInfo) {
        job.setProgress(progress);
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
        //update by peaceWong(2020/05/10) to set jobID to CS
        try {
            if (job.isSucceed()) {
                CSEntranceHelper.registerCSRSData(job);
            }
        } catch (Throwable e) {
            logger.error("Failed to register cs rs data ", e);
        }
        //end update
        updateJobStatus(job);
    }

    private void updateJobStatus(Job job){
        Task task = null;
        if(job.isCompleted()) {
            job.setProgress(1);
        }
        try{
           task = this.entranceContext.getOrCreateEntranceParser().parseToTask(job);
            if (job.isSucceed()){
                //如果是job是成功的，那么需要将task的错误描述等都要设置为null
                ((RequestPersistTask)task).setErrCode(null);
                ((RequestPersistTask)task).setErrDesc(null);
            }
        }catch(ErrorException e){
            entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
            logger.error("update job status failed, reason:", e);
        }
        try {
            createPersistenceEngine().updateIfNeeded(task);
        } catch (ErrorException e) {
            entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
            logger.error("update job status failed, reason: ", e);
        }
    }

}
