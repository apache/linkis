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

package com.webank.wedatasphere.linkis.entrance.job;

import com.webank.wedatasphere.linkis.common.log.LogUtils;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.entrance.execute.*;
import com.webank.wedatasphere.linkis.entrance.log.*;
import com.webank.wedatasphere.linkis.entrance.persistence.HaPersistenceTask;
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask;
import com.webank.wedatasphere.linkis.protocol.task.Task;
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.executer.JobExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.queue.JobInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * created by enjoyyin on 2018/10/17
 * Description:
 */
public class EntranceExecutionJob extends EntranceJob implements LogHandler {

    private LogReader logReader;
    private LogWriter logWriter;
    private WebSocketCacheLogReader webSocketCacheLogReader;
    private WebSocketLogWriter webSocketLogWriter;
    private static final Logger logger = LoggerFactory.getLogger(EntranceExecutionJob.class);

    public class EntranceExecuteRequest implements ExecuteRequest, LockExecuteRequest, JobExecuteRequest, RuntimePropertiesExecuteRequest {

        private String executionCode;

        public void setExecutionCode(){
            Task task = getTask();
            if (task instanceof RequestPersistTask){
                this.executionCode =  ((RequestPersistTask) task).getExecutionCode();
            }
        }

        @Override
        public String code() {
            return this.executionCode;
        }

        @Override
        public String lock() {
            return getLock();
        }

        @Override
        public String jobId() {
            return getId();
        }

        @Override
        public Map<String, Object> properties() {
            Map<String, Object> properties = TaskUtils.getRuntimeMap(getParams());
            if(getTask() instanceof RequestPersistTask) {
                properties.put(TaskConstant.RUNTYPE, ((RequestPersistTask) getTask()).getRunType());
            }
            return properties;
        }
    }

    public class StorePathEntranceExecuteRequest extends EntranceExecuteRequest implements StorePathExecuteRequest {
        @Override
        public String storePath() {
            //TODO storePath should be made an interceptor(storePath应该做成一个拦截器)
            return ((RequestPersistTask) getTask()).getResultLocation();
        }
    }

    //TODO HA method needs to be improved(HA方法还需要再完善)
    public class ReconnectEntranceExecuteRequest extends EntranceExecuteRequest implements ReconnectExecuteRequest {

        @Override
        public String execId() {
            return getTask().getExecId();
        }

        @Override
        public Map<String, Object> properties() {
            Map<String, Object> properties = TaskUtils.getRuntimeMap(getParams());
            RequestPersistTask task = getRequestPersistTask();
            if(task != null) {
                properties.put(TaskConstant.RUNTYPE, task.getRunType());
            }
            return properties;
        }
    }

    public class ReconnectStorePathEntranceExecuteRequest extends ReconnectEntranceExecuteRequest implements StorePathExecuteRequest {
        @Override
        public String storePath() {
            RequestPersistTask task = getRequestPersistTask();
            return task.getResultLocation();
        }
    }


    @Override
    public void setLogReader(LogReader logReader) {
        this.logReader = logReader;
    }

    @Override
    public Option<LogReader> getLogReader() {
        return Option.apply(logReader);
    }

    @Override
    public void setLogWriter(LogWriter logWriter) {
        this.logWriter = logWriter;
    }

    @Override
    public Option<LogWriter> getLogWriter() {
        return Option.apply(logWriter);
    }


    @Override
    public Option<WebSocketCacheLogReader> getWebSocketLogReader() {
        return Option.apply(webSocketCacheLogReader);
    }

    @Override
    public void setWebSocketLogReader(WebSocketCacheLogReader webSocketCacheLogReader) {
        this.webSocketCacheLogReader = webSocketCacheLogReader;
    }

    @Override
    public void setWebSocketLogWriter(WebSocketLogWriter webSocketLogWriter) {
        this.webSocketLogWriter = webSocketLogWriter;
    }

    @Override
    public Option<WebSocketLogWriter> getWebSocketLogWriter() {
        return Option.apply(this.webSocketLogWriter);
    }


    @Override
    public void init() {
        //todo  Job init operation requires something(job的init操作需要一些东西)
    }

    protected RequestPersistTask getRequestPersistTask() {
        if(getTask() instanceof HaPersistenceTask) {
            Task task = ((HaPersistenceTask) getTask()).task();
            if(task instanceof RequestPersistTask) {
                return (RequestPersistTask) task;
            } else {
                return null;
            }
        } else if(getTask() instanceof RequestPersistTask) {
            return (RequestPersistTask) getTask();
        } else {
            return null;
        }
    }

    @Override
    public ExecuteRequest jobToExecuteRequest() {
        EntranceExecuteRequest executeRequest;
        RequestPersistTask task = getRequestPersistTask();
        if(task != null && StringUtils.isNotBlank(task.getResultLocation())) {
            if(getTask() instanceof HaPersistenceTask) {
                executeRequest = new ReconnectStorePathEntranceExecuteRequest();
            } else {
                executeRequest = new StorePathEntranceExecuteRequest();
            }
        } else if(getTask() instanceof HaPersistenceTask) {
            executeRequest = new ReconnectEntranceExecuteRequest();
        } else {
            executeRequest = new EntranceExecuteRequest();
        }
        executeRequest.setExecutionCode();
        return executeRequest;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public JobInfo getJobInfo() {  //TODO You can put this method on LockJob(可以将该方法放到LockJob上去)
        String execID = this.getId();
        String state = this.getState().toString();
        float progress = this.getProgress();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = simpleDateFormat.format(new Date(this.createTime()));
        String startTime = this.startTime() != 0L? simpleDateFormat.format(new Date(this.startTime())):"not started";
        String scheduleTime = this.scheduledTime() != 0L? simpleDateFormat.format(new Date(this.scheduledTime())):"not scheduled";
        String endTime = this.endTime() != 0L? simpleDateFormat.format(new Date(this.endTime())):"on running or not started";
        String runTime;
        if (this.endTime() != 0L){
            runTime = Utils.msDurationToString(this.endTime() - this.createTime());
        }else{
            runTime = "The task did not end normally and the usage time could not be counted.(任务并未正常结束，无法统计使用时间)";
        }
        String metric = "Task creation time(任务创建时间): " + createTime +
                ", Task scheduling time(任务调度时间): " + scheduleTime +
                ", Task start time(任务开始时间): " + startTime +
                ", Mission end time(任务结束时间): " + endTime +
                "\n\n\n" + LogUtils.generateInfo("Your mission(您的任务) " + this.getRequestPersistTask().getTaskID() + " The total time spent is(总耗时时间为): " + runTime);
        return new JobInfo(execID, null, state, progress, metric);
    }

    @Override
    public void close() throws IOException {
        logger.info("job:" + getId() + " is closing");

        //todo  Do a lot of aftercare work when close(close时候要做很多的善后工作)
        if(this.getLogWriter().isDefined()) {
            IOUtils.closeQuietly(this.getLogWriter().get());
        }
        if(this.getLogReader().isDefined()) {
            IOUtils.closeQuietly(getLogReader().get());
        }
    }

}
