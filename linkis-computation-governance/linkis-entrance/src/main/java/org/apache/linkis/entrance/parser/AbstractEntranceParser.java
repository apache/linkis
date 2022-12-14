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

package org.apache.linkis.entrance.parser;

import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.EntranceParser;
import org.apache.linkis.entrance.exception.EntranceErrorCode;
import org.apache.linkis.entrance.exception.EntranceIllegalParamException;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.job.EntranceExecutionJob;
import org.apache.linkis.entrance.persistence.PersistenceManager;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.paser.EmptyCodeParser;
import org.apache.linkis.governance.common.utils.GovernanceConstant;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary.JOBREQ_NOT_NULL;
import static org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary.JOB_NOT_NULL;

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

  protected EntranceJob createEntranceJob() {
    return new EntranceExecutionJob(getPersistenceManager());
  }

  protected PersistenceManager getPersistenceManager() {
    return null;
  }

  /**
   * Parse the executing job into a task, such as operations such as updating the task information
   * in the database.(将正在执行的job解析为一个task，用于诸如更新数据库中task信息等操作)
   *
   * @param job
   * @return
   * @throws EntranceIllegalParamException
   */
  @Override
  public JobRequest parseToJobRequest(Job job) throws EntranceIllegalParamException {
    if (job == null) {
      throw new EntranceIllegalParamException(
          JOB_NOT_NULL.getErrorCode(), JOB_NOT_NULL.getErrorDesc());
    }
    JobRequest jobRequest = ((EntranceJob) job).getJobRequest();
    if (StringUtils.isEmpty(jobRequest.getReqId())) {
      jobRequest.setReqId(job.getId());
    }

    jobRequest.setProgress("" + job.getProgress());
    jobRequest.setStatus(job.getState().toString());
    jobRequest.setUpdatedTime(new Date());
    if (job.isCompleted()
        && !job.isSucceed()
        && job.getErrorResponse() != null
        && StringUtils.isBlank(jobRequest.getErrorDesc())
        && StringUtils.isNotEmpty(job.getErrorResponse().message())) {
      jobRequest.setErrorDesc(job.getErrorResponse().message());
    }
    return jobRequest;
  }

  /**
   * Parse a jobReq into an executable job(将一个task解析成一个可执行的job) todo Parse to jobGroup
   *
   * @param jobReq
   * @return
   */
  @Override
  public Job parseToJob(JobRequest jobReq) throws EntranceIllegalParamException {
    if (jobReq == null) {
      throw new EntranceIllegalParamException(
          JOBREQ_NOT_NULL.getErrorCode(), JOBREQ_NOT_NULL.getErrorDesc());
    }
    EntranceJob job = createEntranceJob();
    job.setId(String.valueOf(jobReq.getId()));
    job.setJobRequest(jobReq);
    job.setUser(jobReq.getExecuteUser());
    Label<?> userCreateLabel =
        jobReq.getLabels().stream()
            .filter(l -> l.getLabelKey().equalsIgnoreCase(LabelKeyConstant.USER_CREATOR_TYPE_KEY))
            .findFirst()
            .orElse(null);
    if (null != userCreateLabel) {
      job.setCreator(userCreateLabel.getStringValue().split("-")[1]);
    } else {
      String msg =
          "JobRequest doesn't hava valid userCreator label. labels : "
              + BDPJettyServerHelper.gson().toJson(jobReq.getLabels());
      logger.error(msg);
      throw new EntranceIllegalParamException(
          EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode(), msg);
    }
    job.setParams(jobReq.getParams());
    Map<String, Object> properties = TaskUtils.getRuntimeMap(job.getParams());
    properties.put(GovernanceConstant.TASK_SOURCE_MAP_KEY(), jobReq.getSource());
    job.setEntranceListenerBus(entranceContext.getOrCreateEventListenerBus());
    job.setEntranceContext(entranceContext);
    job.setListenerEventBus(null);
    job.setProgress(0f);
    job.setJobRequest(jobReq);
    job.setCodeParser(new EmptyCodeParser());
    return job;
  }
}
