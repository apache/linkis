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
 
package org.apache.linkis.entrance.job;

import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.exception.EntranceErrorCode;
import org.apache.linkis.entrance.exception.EntranceErrorException;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.execute.EntranceJob$;
import org.apache.linkis.entrance.log.*;
import org.apache.linkis.entrance.persistence.PersistenceManager;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.entity.job.SubJobDetail;
import org.apache.linkis.governance.common.entity.job.SubJobInfo;
import org.apache.linkis.governance.common.protocol.task.RequestTask;
import org.apache.linkis.governance.common.protocol.task.RequestTask$;
import org.apache.linkis.governance.common.utils.GovernanceConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.entrance.BindEngineLabel;
import org.apache.linkis.orchestrator.plans.ast.QueryParams$;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.linkis.scheduler.executer.ExecuteRequest;
import org.apache.linkis.scheduler.queue.JobInfo;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class EntranceExecutionJob extends EntranceJob implements LogHandler {

    private LogReader logReader;
    private LogWriter logWriter;
    private WebSocketCacheLogReader webSocketCacheLogReader;
    private WebSocketLogWriter webSocketLogWriter;
    private static final Logger logger = LoggerFactory.getLogger(EntranceExecutionJob.class);
    private PersistenceManager persistenceManager;
    private int runningIndex = 0;

    public EntranceExecutionJob(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /*public class StorePathEntranceExecuteRequest extends EntranceExecuteRequest implements StorePathExecuteRequest {
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
    }*/


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
    public void init() throws EntranceErrorException {
        List<EntranceErrorException> errList = new ArrayList<>();
        SubJobInfo[] subJobInfos = Arrays.stream(getCodeParser().parse(getJobRequest().getExecutionCode())).map(code -> {
            SubJobInfo subJobInfo = new SubJobInfo();
            subJobInfo.setJobReq(getJobRequest());
            subJobInfo.setStatus(SchedulerEventState.Inited().toString());
            subJobInfo.setCode(code);
            // persist and update jobDetail
            SubJobDetail subJobDetail = createNewJobDetail();
            subJobInfo.setSubJobDetail(subJobDetail);
            subJobInfo.setProgress(0.0f);
            subJobDetail.setExecutionContent(code);
            subJobDetail.setJobGroupId(getJobRequest().getId());
            subJobDetail.setStatus(SchedulerEventState.Inited().toString());
            subJobDetail.setCreatedTime(new Date(System.currentTimeMillis()));
            subJobDetail.setUpdatedTime(new Date(System.currentTimeMillis()));
            try {
                persistenceManager.createPersistenceEngine().persist(subJobInfo);
            } catch (Exception e1) {
                errList.add(new EntranceErrorException(EntranceErrorCode.INIT_JOB_ERROR.getErrCode(), "Init subjob error, please submit it again(任务初始化失败，请稍后重试). " + e1.getMessage()));
            }
            return subJobInfo;
        }).toArray(SubJobInfo[]::new);
        if (errList.size() > 0) {
            logger.error(errList.get(0).getDesc());
            throw errList.get(0);
        }
        setJobGroups(subJobInfos);
    }

    /*protected RequestPersistTask getRequestPersistTask() {
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
    }*/

    @Override
    public SubJobInfo getRunningSubJob() {
        if (runningIndex < getJobGroups().length) {
            return getJobGroups()[runningIndex];
        } else {
            return null;
        }
    }

    @Override
    public ExecuteRequest jobToExecuteRequest() throws EntranceErrorException {
        // add resultSet path root
        Map<String, String> starupMapTmp = new HashMap<String, String>();
        Map<String, Object> starupMapOri = TaskUtils.getStartupMap(getParams());
        for (Map.Entry<String, Object> entry : starupMapOri.entrySet()) {
            if (null != entry.getKey() && null != entry.getValue()) {
                starupMapTmp.put(entry.getKey(), entry.getValue().toString());
            }
        }
        Map<String, Object> runtimeMapOri = TaskUtils.getRuntimeMap(getParams());
        if (null == runtimeMapOri || runtimeMapOri.isEmpty()) {
            TaskUtils.addRuntimeMap(getParams(), new HashMap<>());
            runtimeMapOri = TaskUtils.getRuntimeMap(getParams());
        }
        Map<String, String> runtimeMapTmp = new HashMap<>();
        for (Map.Entry<String, Object> entry : runtimeMapOri.entrySet()) {
            if (null != entry.getKey() && null != entry.getValue()) {
                runtimeMapTmp.put(entry.getKey(), entry.getValue().toString());
            }
        }
        String resultSetPathRoot = GovernanceCommonConf.RESULT_SET_STORE_PATH().getValue(runtimeMapTmp);
        Map<String, Object> jobMap = new HashMap<String, Object>();
        jobMap.put(RequestTask$.MODULE$.RESULT_SET_STORE_PATH(), resultSetPathRoot);
        runtimeMapOri.put(QueryParams$.MODULE$.JOB_KEY(), jobMap);

        EntranceExecuteRequest executeRequest = new EntranceExecuteRequest(this);
        boolean isCompleted = true;
        boolean isHead = false;
        boolean isTail = false;
        if (null != jobGroups() && jobGroups().length > 0) {
            for (int i = 0; i < jobGroups().length; i++) {
                if (null != jobGroups()[i].getSubJobDetail()) {
                    SubJobDetail subJobDetail = jobGroups()[i].getSubJobDetail();
                    if (SchedulerEventState.isCompletedByStr(subJobDetail.getStatus())) {
                        continue;
                    } else {
                        isCompleted = false;
                        executeRequest.setExecutionCode(i);
                        runningIndex = i;
                        subJobDetail.setPriority(i);
                        break;
                    }
                } else {
                    throw new EntranceErrorException(EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode(), "Subjob was not inited, please submit again.");
                }
            }
            if (0 == runningIndex) {
                isHead = true;
            } else {
                isHead = false;
            }
            if (runningIndex >= jobGroups().length - 1) {
                isTail = true;
            } else {
                isTail = false;
            }
        } else {
            isHead = true;
            isTail = true;
        }
        BindEngineLabel bindEngineLabel = new BindEngineLabel()
                .setJobGroupId(getJobRequest().getId().toString())
                .setIsJobGroupHead(String.valueOf(isHead))
                .setIsJobGroupEnd(String.valueOf(isTail));
        if (isHead) {
            jobMap.put(GovernanceConstant.RESULTSET_INDEX(), 0);
            setResultSize(0);
        } else {
            jobMap.put(GovernanceConstant.RESULTSET_INDEX(), addAndGetResultSize(0));
        }
        List<Label<?>> labels = new ArrayList<Label<?>>();
        labels.addAll(getJobRequest().getLabels());
        labels.add(bindEngineLabel);
        executeRequest.setLables(labels);
        if (isCompleted) {
            return null;
        } else {
            return executeRequest;
        }
    }

    private SubJobDetail createNewJobDetail() {
        SubJobDetail subJobDetail = new SubJobDetail();
        subJobDetail.setUpdatedTime(subJobDetail.getCreatedTime());
        subJobDetail.setJobGroupId(getJobRequest().getId());
        subJobDetail.setStatus(SchedulerEventState.Scheduled().toString());
        subJobDetail.setJobGroupInfo("");
        return subJobDetail;
    }

    @Override
    public String getName() {
        return "taskID:" + String.valueOf(getJobRequest().getId()) + "execID:" + getId();
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public JobInfo getJobInfo() {  //TODO You can put this method on LockJob(可以将该方法放到LockJob上去)
        String execID = getId();
        String state = this.getState().toString();
        float progress = this.getProgress();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(getJobRequest().getMetrics() == null){
            getJobRequest().setMetrics(new HashMap<>());
        }
        Map<String, Object> metricsMap = getJobRequest().getMetrics();
        String createTime = metricsMap.containsKey(TaskConstant.ENTRANCEJOB_SUBMIT_TIME) ? simpleDateFormat.format(metricsMap.get(TaskConstant.ENTRANCEJOB_SUBMIT_TIME)) : "not created";
        String scheduleTime = metricsMap.containsKey(TaskConstant.ENTRANCEJOB_SCHEDULE_TIME) ? simpleDateFormat.format(metricsMap.get(TaskConstant.ENTRANCEJOB_SCHEDULE_TIME)) : "not scheduled";
        String startTime = metricsMap.containsKey(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR) ? simpleDateFormat.format(metricsMap.get(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR)) : "not submitted to orchestrator";
        String endTime = metricsMap.containsKey(TaskConstant.ENTRANCEJOB_COMPLETE_TIME) ? simpleDateFormat.format(metricsMap.get(TaskConstant.ENTRANCEJOB_COMPLETE_TIME)) : "on running or not started";
        String runTime;
        if (metricsMap.containsKey(TaskConstant.ENTRANCEJOB_COMPLETE_TIME)){
            runTime = Utils.msDurationToString((((Date) metricsMap.get(TaskConstant.ENTRANCEJOB_COMPLETE_TIME))).getTime()
            - (((Date) metricsMap.get(TaskConstant.ENTRANCEJOB_SUBMIT_TIME))).getTime());
        }else{
            runTime = "The task did not end normally and the usage time could not be counted.(任务并未正常结束，无法统计使用时间)";
        }
        String metric = "Task creation time(任务创建时间): " + createTime +
                ", Task scheduling time(任务调度时间): " + scheduleTime +
                ", Task start time(任务开始时间): " + startTime +
                ", Mission end time(任务结束时间): " + endTime +
                "\n\n\n" + LogUtils.generateInfo("Your mission(您的任务) " + this.getJobRequest().getId() + " The total time spent is(总耗时时间为): " + runTime);
        return new JobInfo(execID, null, state, progress, metric);
    }

    @Override
    public void close() throws IOException {
        logger.info("job:" + id() + " is closing");

        try {
            //todo  Do a lot of aftercare work when close(close时候要做很多的善后工作)
            if(this.getLogWriter().isDefined()) {
                IOUtils.closeQuietly(this.getLogWriter().get());
            }
            if(this.getLogReader().isDefined()) {
                IOUtils.closeQuietly(getLogReader().get());
            }
        } catch (Exception e) {
            logger.warn("Close logWriter and logReader failed. {}", e.getMessage(), e);
        }
    }

    @Override
    public float getProgress() {
        float progress = super.getProgress();
        SubJobInfo[] subJobInfoArray;
        if(progress < 1.0 && (subJobInfoArray = getJobGroups()).length > 0){
            int groupCount = subJobInfoArray.length;
            float progressValueSum = 0.0f;
            for(SubJobInfo subJobInfo : subJobInfoArray){
                progressValueSum += subJobInfo.getProgress();
            }
            return progressValueSum /(float)groupCount;
        }
        return progress;
    }

    /**
     *  // The front end needs to obtain data
     *  //if (EntranceJob.JOB_COMPLETED_PROGRESS() == getProgress()) {
     *  //    return new JobProgressInfo[0];
     *  //}
     * @return
     */
    @Override
    public JobProgressInfo[] getProgressInfo() {
        SubJobInfo[] subJobInfoArray = getJobGroups();
        if(subJobInfoArray.length > 0){
            List<JobProgressInfo> progressInfoList = new ArrayList<>();
            for(SubJobInfo subJobInfo : subJobInfoArray){
                progressInfoList.addAll(subJobInfo.getProgressInfoMap().values());
            }
            return progressInfoList.toArray(new JobProgressInfo[]{});
        }
        return super.getProgressInfo();
    }
}
