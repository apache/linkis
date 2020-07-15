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

package com.webank.wedatasphere.linkis.entrance.parser;

import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.EntranceParser;
import com.webank.wedatasphere.linkis.entrance.exception.EntranceIllegalParamException;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob;
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask;
import com.webank.wedatasphere.linkis.protocol.task.Task;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * created by enjoyyin on 2018/10/11
 * Description:
 */
public abstract class AbstractEntranceParser extends EntranceParser {

    private EntranceContext entranceContext;

    private static final Logger logger = LoggerFactory.getLogger(AbstractEntranceParser.class);

    @Override
    public EntranceContext getEntranceContext() {
        return entranceContext;
    }

    @Override
    public void setEntranceContext(EntranceContext entranceContext) {
        this.entranceContext = entranceContext;
    }

    /**
     * Parse the executing job into a task, such as operations such as updating the task information in the database.(将正在执行的job解析为一个task，用于诸如更新数据库中task信息等操作)
     * @param job
     * @return
     * @throws EntranceIllegalParamException
     */
    @Override
    public Task parseToTask(Job job) throws EntranceIllegalParamException{
        if (job == null){
            throw new EntranceIllegalParamException(20002, "job can't be null");
        }
        Task task = ((EntranceJob)job).getTask();
        if(StringUtils.isEmpty(task.getExecId())) {
            task.setExecId(job.getId());
        }
        if (task instanceof RequestPersistTask){
            ((RequestPersistTask) task).setProgress(job.getProgress());
            ((RequestPersistTask) task).setStatus(job.getState().toString());
            ((RequestPersistTask) task).setUpdatedTime(new Date(System.currentTimeMillis()));
            ((RequestPersistTask) task).setProgress(job.getProgress());
            if(job.isCompleted() && !job.isSucceed() && job.getErrorResponse() != null
                    && StringUtils.isBlank(((RequestPersistTask) task).getErrDesc())
                && StringUtils.isNotEmpty(job.getErrorResponse().message())) {
                ((RequestPersistTask) task).setErrDesc(job.getErrorResponse().message());
            }
            //if job is successful, errCode and errDesc needs to be null
            if (job.isSucceed()){
                ((RequestPersistTask) task).setErrCode(null);
                ((RequestPersistTask) task).setErrDesc(null);
            }
        }else{
            logger.warn("not supported task type");
        }
        return task;
    }

    protected EntranceJob createEntranceJob() {
        return new EntranceExecutionJob();
    }

    /**
     * Parse a task into an executable job(将一个task解析成一个可执行的job)
     * @param task
     * @return
     */
    @Override
    public Job parseToJob(Task task) throws EntranceIllegalParamException {
        if (task == null){
            throw new EntranceIllegalParamException(20001, "task can't be null");
        }
        EntranceJob job = null;

        if (task instanceof RequestPersistTask){
            job = createEntranceJob();
            job.setTask(task);
            job.setUser(((RequestPersistTask) task).getUmUser());
            job.setCreator(((RequestPersistTask) task).getRequestApplicationName());
            job.setParams(((RequestPersistTask) task).getParams());
            //job.setLogListener(entranceContext.getOrCreateLogManager());
            //job.setProgressListener(entranceContext.getOrCreatePersistenceManager());
            //job.setJobListener(entranceContext.getOrCreatePersistenceManager());
            job.setEntranceListenerBus(entranceContext.getOrCreateEventListenerBus());
            job.setEntranceContext(entranceContext);
            job.setListenerEventBus(null);
            job.setProgress(0f);
        }
        return job;
    }

}
