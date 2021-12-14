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

import java.util.Date;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.conf.EntranceConfiguration$;
import org.apache.linkis.entrance.exception.EntranceErrorCode;
import org.apache.linkis.entrance.exception.EntranceIllegalParamException;
import org.apache.linkis.entrance.exception.EntranceRPCException;
import org.apache.linkis.entrance.exception.QueryFailedException;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.governance.common.entity.job.SubJobDetail;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.entity.job.SubJobInfo;
import org.apache.linkis.governance.common.entity.task.*;
import org.apache.linkis.governance.common.protocol.job.*;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.protocol.task.Task;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryPersistenceEngine extends AbstractPersistenceEngine{

    private Sender sender;

    private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceEngine.class);
    private static final int MAX_DESC_LEN = 320;

    private static final int RETRY_NUMBER = EntranceConfiguration.JOBINFO_UPDATE_RETRY_MAX_TIME().getValue();

    public QueryPersistenceEngine(){
        /*
            Get the corresponding sender through datawork-linkis-publicservice(通过datawork-linkis-publicservice 拿到对应的sender)
         */
         sender = Sender.getSender(EntranceConfiguration$.MODULE$.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME().getValue());
    }


    @Override
    public void persist(SubJobInfo subJobInfo) throws QueryFailedException, EntranceIllegalParamException {
        if (null == subJobInfo || null == subJobInfo.getSubJobDetail()) {
            throw new EntranceIllegalParamException(20004, "JobDetail can not be null, unable to do persist operation");
        }
        JobDetailReqInsert jobReqInsert = new JobDetailReqInsert(subJobInfo);
        JobRespProtocol jobRespProtocol = sendToJobHistoryAndRetry(jobReqInsert, "subJobInfo of job" + subJobInfo.getJobReq().getId());
        if (jobRespProtocol != null) {
            Map<String, Object> data = jobRespProtocol.getData();
            Object object = data.get(JobRequestConstants.JOB_ID());
            if (object == null) {
                throw new QueryFailedException(20011, "Insert jobDetail failed, reason: " + jobRespProtocol.getMsg());
            }
            String jobIdStr = object.toString();
            Long jobId = Long.parseLong(jobIdStr);
            subJobInfo.getSubJobDetail().setId(jobId);
        }
    }

    private JobRespProtocol sendToJobHistoryAndRetry(RequestProtocol jobReq, String msg) throws QueryFailedException {
        JobRespProtocol jobRespProtocol = null;
        int retryTimes = 0;
        boolean retry = true;
        while (retry && retryTimes < RETRY_NUMBER) {
            try {
                retryTimes++;
                jobRespProtocol = (JobRespProtocol) sender.ask(jobReq);
                if (jobRespProtocol.getStatus() == 2) {
                    logger.warn("Request jobHistory failed, joReq msg{}, retry times: {}, reason {}", msg, retryTimes, jobRespProtocol.getMsg());
                } else {
                    retry = false;
                }
            } catch (Exception e) {
                logger.warn("Request jobHistory failed, joReq msg{}, retry times: {}, reason {}", msg, retryTimes, e);
            }
            if (retry) {
                try {
                    Thread.sleep(EntranceConfiguration.JOBINFO_UPDATE_RETRY_INTERVAL().getValue());
                } catch (Exception ex) {
                    logger.warn(ex.getMessage());
                }
            }
        }
        if (jobRespProtocol != null) {
            int status = jobRespProtocol.getStatus();
            String message = jobRespProtocol.getMsg();
            if (status != 0) {
                throw new QueryFailedException(20011, "Request jobHistory failed, reason: " + message);
            }
        } else {
            throw new QueryFailedException(20011, "Request jobHistory failed, reason: jobRespProtocol is null ");
        }
        return jobRespProtocol;
    }

    @Override
    public void updateIfNeeded(JobRequest jobReq) throws ErrorException, QueryFailedException {
        if (null == jobReq) {
            throw new EntranceIllegalParamException(20004, "JobReq cannot be null.");
        }
        JobRequest jobReqForUpdate = new JobRequest();
        BeanUtils.copyProperties(jobReq, jobReqForUpdate);
        if (null != jobReq.getErrorDesc()) {
            // length of errorDesc must be less then 1000 / 3, because length of errorDesc in db is 1000
            if (jobReq.getErrorDesc().length() > MAX_DESC_LEN) {
                jobReqForUpdate.setErrorDesc(jobReq.getErrorDesc().substring(0, MAX_DESC_LEN));
            }
        }
        jobReqForUpdate.setUpdatedTime(new Date());
        JobReqUpdate jobReqUpdate = new JobReqUpdate(jobReqForUpdate);
        JobRespProtocol jobRespProtocol = sendToJobHistoryAndRetry(jobReqUpdate, "job:" + jobReq.getReqId() + "status:" + jobReq.getStatus());
    }

    @Override
    public SubJobDetail retrieveJobDetailReq(Long jobDetailId)throws EntranceIllegalParamException, QueryFailedException, EntranceRPCException{

        if ( jobDetailId == null ||  jobDetailId < 0){
            throw new EntranceIllegalParamException(20003, "taskID can't be null or less than 0");
        }
        SubJobDetail subJobDetail = new SubJobDetail();
        subJobDetail.setId(jobDetailId);
        JobDetailReqQuery jobDetailReqQuery = new JobDetailReqQuery(subJobDetail);
        ResponseOneJobDetail responseOneJobDetail = null;
        try {
            responseOneJobDetail = (ResponseOneJobDetail) sender.ask(jobDetailReqQuery);
            if (null != responseOneJobDetail) {
                return responseOneJobDetail.jobDetail();
            }
        }catch(Exception e){
            logger.error("Requesting the corresponding jobDetail failed with jobDetailId: {}(通过jobDetailId: {} 请求相应的task失败)", jobDetailId, jobDetailId, e);
            throw new EntranceRPCException(20020, "sender rpc failed", e);
        }
        return null;
    }

    @Override
    public void persist(JobRequest jobReq) throws ErrorException {
        if (null == jobReq) {
            throw new EntranceIllegalParamException(20004, "JobRequest cannot be null, unable to do persist operation");
        }
        JobReqInsert jobReqInsert = new JobReqInsert(jobReq);
        JobRespProtocol jobRespProtocol = sendToJobHistoryAndRetry(jobReqInsert, "Insert job");
        if (null != jobRespProtocol) {
            Map<String, Object> data = jobRespProtocol.getData();
            Object object = data.get(JobRequestConstants.JOB_ID());
            if (null == object) {
                throw new QueryFailedException(20011, "Insert JobReq failed, reason : " + jobRespProtocol.getMsg());
            }
            String jobIdStr = object.toString();
            Long jobId = Long.parseLong(jobIdStr);
            jobReq.setId(jobId);
        }
    }


    @Override
    public void updateIfNeeded(SubJobInfo subJobInfo) throws QueryFailedException, EntranceIllegalParamException{
        if (null == subJobInfo || null == subJobInfo.getSubJobDetail()) {
            throw new EntranceIllegalParamException(20004, "task can not be null, unable to do update operation");
        }
        JobDetailReqUpdate jobDetailReqUpdate = new JobDetailReqUpdate(subJobInfo);
        jobDetailReqUpdate.jobInfo().getSubJobDetail().setUpdatedTime(new Date(System.currentTimeMillis()));
        JobRespProtocol jobRespProtocol = sendToJobHistoryAndRetry(jobDetailReqUpdate, "jobDetail:" + subJobInfo.getSubJobDetail().getId() + "status:" + subJobInfo.getStatus());
    }

    @Override
    public Task[] readAll(String instance)throws EntranceIllegalParamException, EntranceRPCException,QueryFailedException{

        List<Task> retList = new ArrayList<>();

        if (instance == null || "".equals(instance)){
            throw new EntranceIllegalParamException(20004, "instance can not be null");
        }

        RequestReadAllTask requestReadAllTask = new RequestReadAllTask(instance);
        ResponsePersist responsePersist = null;
        try{
            responsePersist = (ResponsePersist)sender.ask(requestReadAllTask);
        }catch(Exception e){
            throw new EntranceRPCException(20020, "sender rpc failed ", e);
        }
        if (responsePersist != null){
            int status = responsePersist.getStatus();
            String message = responsePersist.getMsg();
            if (status != 0){
                throw new QueryFailedException(20011, "read all tasks failed, reason: " + message);
            }
            Map<String, Object> data = responsePersist.getData();
            Object object = data.get(TaskConstant.TASK);
            if( object instanceof List){
                List list = (List)object;
                if (list.size() == 0){
                    logger.info("no running task in this instance: {}", instance);
                }
                for(Object o : list){
                    if (o instanceof RequestPersistTask ){
                        retList.add((RequestPersistTask)o);
                    }
                }
            }
        }
        return retList.toArray(new Task[0]);
    }

    @Override
    public JobRequest retrieveJobReq(Long jobGroupId) throws ErrorException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }
}
