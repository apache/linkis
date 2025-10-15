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

package org.apache.linkis.entrance.persistence;

import org.apache.linkis.common.conf.Configuration$;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.exception.EntranceIllegalParamException;
import org.apache.linkis.entrance.exception.EntranceRPCException;
import org.apache.linkis.entrance.exception.QueryFailedException;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.governance.common.entity.task.RequestReadAllTask;
import org.apache.linkis.governance.common.entity.task.ResponsePersist;
import org.apache.linkis.governance.common.protocol.job.JobReqInsert;
import org.apache.linkis.governance.common.protocol.job.JobReqUpdate;
import org.apache.linkis.governance.common.protocol.job.JobRespProtocol;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.protocol.task.Task;
import org.apache.linkis.rpc.Sender;

import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary.*;

public class QueryPersistenceEngine extends AbstractPersistenceEngine {

  private Sender sender;

  private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceEngine.class);
  private static final int MAX_DESC_LEN = GovernanceCommonConf.ERROR_CODE_DESC_LEN();

  public QueryPersistenceEngine() {
    /*
       Get the corresponding sender through datawork-linkis-publicservice(通过datawork-linkis-publicservice 拿到对应的sender)
    */
    sender =
        Sender.getSender(Configuration$.MODULE$.JOBHISTORY_SPRING_APPLICATION_NAME().getValue());
  }

  private JobRespProtocol sendToJobHistoryAndRetry(RequestProtocol jobReq, String msg)
      throws QueryFailedException {
    JobRespProtocol jobRespProtocol = null;
    int retryTimes = 0;
    boolean retry = true;
    while (retry
        && retryTimes < EntranceConfiguration.JOBINFO_UPDATE_RETRY_MAX_TIME().getHotValue()) {
      try {
        retryTimes++;
        jobRespProtocol = (JobRespProtocol) sender.ask(jobReq);
        if (jobRespProtocol.getStatus() == 2) {
          logger.warn(
              "Request jobHistory failed, joReq msg{}, retry times: {}, reason {}",
              msg,
              retryTimes,
              jobRespProtocol.getMsg());
        } else {
          retry = false;
        }
      } catch (Exception e) {
        logger.warn(
            "Request jobHistory failed, joReq msg{}, retry times: {}, reason {}",
            msg,
            retryTimes,
            e);
      }
      if (retry) {
        try {
          Thread.sleep(EntranceConfiguration.JOBINFO_UPDATE_RETRY_INTERVAL().getHotValue());
        } catch (Exception ex) {
          logger.warn(ex.getMessage());
        }
      }
    }
    if (jobRespProtocol != null) {
      int status = jobRespProtocol.getStatus();
      String message = jobRespProtocol.getMsg();
      if (status != 0) {
        throw new QueryFailedException(
            REQUEST_JOBHISTORY_FAILED.getErrorCode(),
            MessageFormat.format(REQUEST_JOBHISTORY_FAILED.getErrorDesc(), message));
      }
    } else {
      throw new QueryFailedException(
          JOBRESP_PROTOCOL_NULL.getErrorCode(), JOBRESP_PROTOCOL_NULL.getErrorDesc());
    }
    return jobRespProtocol;
  }

  @Override
  public void updateIfNeeded(JobRequest jobReq) throws ErrorException, QueryFailedException {
    if (null == jobReq) {
      throw new EntranceIllegalParamException(
          JOBREQ_NOT_NULL.getErrorCode(), JOBREQ_NOT_NULL.getErrorDesc());
    }
    JobRequest jobReqForUpdate = new JobRequest();
    BeanUtils.copyProperties(jobReq, jobReqForUpdate);
    if (null != jobReq.getErrorDesc()) {
      // length of errorDesc must be less then 1000 / 3, because length of errorDesc in db is
      // 1000
      if (jobReq.getErrorDesc().length() > MAX_DESC_LEN) {
        jobReqForUpdate.setErrorDesc(jobReq.getErrorDesc().substring(0, MAX_DESC_LEN));
      }
    }
    jobReqForUpdate.setUpdatedTime(new Date());
    JobReqUpdate jobReqUpdate = new JobReqUpdate(jobReqForUpdate);
    JobRespProtocol jobRespProtocol =
        sendToJobHistoryAndRetry(
            jobReqUpdate, "job:" + jobReq.getReqId() + "status:" + jobReq.getStatus());
  }

  @Override
  public void persist(JobRequest jobReq) throws ErrorException {
    if (null == jobReq) {
      throw new EntranceIllegalParamException(
          JOBREQUEST_NOT_NULL.getErrorCode(), JOBREQUEST_NOT_NULL.getErrorDesc());
    }
    if (logger.isDebugEnabled()) {
      try {
        logger.debug("jobReq:" + JsonUtils.jackson().writeValueAsString(jobReq));
      } catch (JsonProcessingException e) {
        logger.debug("convert jobReq to string with error:" + e.getMessage());
      }
    }

    JobReqInsert jobReqInsert = new JobReqInsert(jobReq);

    JobRespProtocol jobRespProtocol = sendToJobHistoryAndRetry(jobReqInsert, "Insert job");
    if (null != jobRespProtocol) {
      Map<String, Object> data = jobRespProtocol.getData();
      Object object = data.get(JobRequestConstants.JOB_ID());
      if (null == object) {
        throw new QueryFailedException(
            20011, "Insert JobReq failed, reason : " + jobRespProtocol.getMsg());
      }
      String jobIdStr = object.toString();
      Long jobId = Long.parseLong(jobIdStr);
      jobReq.setId(jobId);
    }
  }

  @Override
  public Task[] readAll(String instance)
      throws EntranceIllegalParamException, EntranceRPCException, QueryFailedException {

    List<Task> retList = new ArrayList<>();

    if (instance == null || "".equals(instance)) {
      throw new EntranceIllegalParamException(
          INSTANCE_NOT_NULL.getErrorCode(), INSTANCE_NOT_NULL.getErrorDesc());
    }

    RequestReadAllTask requestReadAllTask = new RequestReadAllTask(instance);
    ResponsePersist responsePersist = null;
    try {
      responsePersist = (ResponsePersist) sender.ask(requestReadAllTask);
    } catch (Exception e) {
      throw new EntranceRPCException(
          SENDER_RPC_FAILED.getErrorCode(), SENDER_RPC_FAILED.getErrorDesc(), e);
    }
    if (responsePersist != null) {
      int status = responsePersist.getStatus();
      String message = responsePersist.getMsg();
      if (status != 0) {
        throw new QueryFailedException(
            READ_TASKS_FAILED.getErrorCode(),
            MessageFormat.format(READ_TASKS_FAILED.getErrorDesc(), message));
      }
      Map<String, Object> data = responsePersist.getData();
      Object object = data.get(TaskConstant.TASK);
      if (object instanceof List) {
        List list = (List) object;
        if (list.size() == 0) {
          logger.info("no running task in this instance: {}", instance);
        }
        for (Object o : list) {
          if (o instanceof RequestPersistTask) {
            retList.add((RequestPersistTask) o);
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
  public void close() throws IOException {}

  @Override
  public void flush() throws IOException {}
}
