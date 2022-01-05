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
 
package org.apache.linkis.jobhistory.conversions

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.job.{JobRequest, SubJobDetail}
import org.apache.linkis.governance.common.entity.task.{RequestPersistTask, RequestQueryTask}
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration
import org.apache.linkis.jobhistory.entity.{JobDetail, JobHistory, QueryTask, QueryTaskVO}
import org.apache.linkis.jobhistory.transitional.TaskStatus
import org.apache.linkis.jobhistory.util.QueryUtils
import org.apache.linkis.manager.label.builder.factory.{LabelBuilderFactory, LabelBuilderFactoryContext}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.ZuulEntranceUtils
import org.apache.linkis.server.{BDPJettyServerHelper, toScalaBuffer, toScalaMap}
import org.apache.commons.lang.StringUtils
import org.springframework.beans.BeanUtils
import java.util
import java.util.Date
import java.text.SimpleDateFormat

import org.apache.linkis.protocol.constants.TaskConstant

import scala.collection.JavaConverters.{asJavaIterableConverter, asScalaBufferConverter, mapAsScalaMapConverter}


object TaskConversions extends Logging {

  lazy private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  @Deprecated
  def requestQueryTask2QueryTask(requestQueryTask: RequestQueryTask): QueryTask = {
    val task: QueryTask = new QueryTask
    BeanUtils.copyProperties(requestQueryTask, task)
    if (requestQueryTask.getParams != null)
      task.setParamsJson(BDPJettyServerHelper.gson.toJson(requestQueryTask.getParams))
    else
      task.setParamsJson(null)
    task
  }

  /*@Deprecated
  def queryTask2RequestPersistTask(queryTask: QueryTask): RequestPersistTask = {
    QueryUtils.exchangeExecutionCode(queryTask)
    val task = new RequestPersistTask
    BeanUtils.copyProperties(queryTask, task)
    task.setSource(BDPJettyServerHelper.gson.fromJson(queryTask.getSourceJson, classOf[java.util.HashMap[String, String]]))
    task.setParams(BDPJettyServerHelper.gson.fromJson(queryTask.getParamsJson, classOf[java.util.HashMap[String, Object]]))
    task
  }*/

  /*@Deprecated
  def requestPersistTaskTask2QueryTask(requestPersistTask: RequestPersistTask): QueryTask = {
    val task: QueryTask = new QueryTask
    BeanUtils.copyProperties(requestPersistTask, task)
    if (requestPersistTask.getParams != null)
      task.setParamsJson(BDPJettyServerHelper.gson.toJson(requestPersistTask.getParams))
    else
      task.setParamsJson(null)
    task
  }*/

  /*def queryTask2QueryTaskVO(queryTask: QueryTask): QueryTaskVO = {
    QueryUtils.exchangeExecutionCode(queryTask)
    val taskVO = new QueryTaskVO
    BeanUtils.copyProperties(queryTask, taskVO)
    if (!StringUtils.isEmpty(taskVO.getSourceJson)) {
      Utils.tryCatch {
        val source = BDPJettyServerHelper.gson.fromJson(taskVO.getSourceJson, classOf[util.Map[String, String]])
        taskVO.setSourceTailor(source.asScala.map(_._2).foldLeft("")(_ + _ + "-").stripSuffix("-"))
      } {
        case _ => warn("sourceJson deserializae failed,this task may be the old data")
      }
    }
    if (queryTask.getExecId() != null && queryTask.getExecuteApplicationName() != null && queryTask.getInstance() != null) {
      taskVO.setStrongerExecId(ZuulEntranceUtils.generateExecID(queryTask.getExecId(),
        queryTask.getExecuteApplicationName(), queryTask.getInstance(), queryTask.getRequestApplicationName))
    }
    val status = queryTask.getStatus()
    val createdTime = queryTask.getCreatedTime()
    val updatedTime = queryTask.getUpdatedTime()
    if (isJobFinished(status) && createdTime != null && updatedTime != null) {
      taskVO.setCostTime(queryTask.getUpdatedTime().getTime() - queryTask.getCreatedTime().getTime());
    } else if (createdTime != null) {
      taskVO.setCostTime(System.currentTimeMillis() - queryTask.getCreatedTime().getTime());
    }
    taskVO
  }*/

  def isJobFinished(status: String): Boolean = {
    TaskStatus.Succeed.toString.equals(status) ||
      TaskStatus.Failed.toString.equals(status) ||
      TaskStatus.Cancelled.toString.equals(status) ||
      TaskStatus.Timeout.toString.equals(status)
  }

  def jobHistory2JobRequest(jobHistoryList: util.List[JobHistory]): util.List[JobRequest] = {
    val jobRequestList = new util.ArrayList[JobRequest](jobHistoryList.size())
    jobHistoryList.asScala.foreach(job => {
      val jobReq: JobRequest = jobHistory2JobRequest(job)
      jobRequestList.add(jobReq)
    })
    jobRequestList
  }

  def jobHistory2JobRequest(job: JobHistory): JobRequest = {
    if (null == job) return null
    val jobReq = new JobRequest
    jobReq.setId(job.getId)
    jobReq.setReqId(job.getJob_req_id)
    //    jobReq.setPriority(job.getPriority)
    jobReq.setSubmitUser(job.getSubmit_user)
    jobReq.setExecuteUser(job.getExecute_user)
    if (null != job.getSource) jobReq.setSource(BDPJettyServerHelper.gson.fromJson(job.getSource, classOf[util.Map[String, Object]]))
    if (null != job.getLabels) jobReq.setLabels(getLabelListFromJson(job.getLabels))
    jobReq.setParams(BDPJettyServerHelper.gson.fromJson(job.getParams, classOf[util.Map[String, Object]]))
    jobReq.setProgress(job.getProgress)
    jobReq.setStatus(job.getStatus)
    jobReq.setLogPath(job.getLog_path)
    jobReq.setErrorCode(job.getError_code)
    jobReq.setErrorDesc(job.getError_desc)
    jobReq.setCreatedTime(job.getCreated_time)
    jobReq.setUpdatedTime(job.getUpdated_time)
    jobReq.setMetrics(BDPJettyServerHelper.gson.fromJson((job.getMetrics), classOf[util.Map[String, Object]]))
    jobReq.setInstances(job.getInstances)
    QueryUtils.exchangeExecutionCode(job)
    jobReq.setExecutionCode(job.getExecution_code)
    jobReq
  }

  def jobRequest2JobHistory(jobReq: JobRequest): JobHistory = {
    if (null == jobReq) return null
    val jobHistory = new JobHistory
    jobHistory.setId(jobReq.getId)
    jobHistory.setJob_req_id(jobReq.getReqId)
    //    jobHistory.setPriority(jobReq.getProgress)
    jobHistory.setSubmit_user(jobReq.getSubmitUser)
    jobHistory.setExecute_user(jobReq.getExecuteUser)
    jobHistory.setSource(BDPJettyServerHelper.gson.toJson(jobReq.getSource))
    if (null != jobReq.getLabels) {
      val labelMap = new util.HashMap[String, String](jobReq.getLabels.size())
      jobReq.getLabels.asScala.map(l => l.getLabelKey -> l.getStringValue).foreach(kv => labelMap.put(kv._1, kv._2))
      jobHistory.setLabels(BDPJettyServerHelper.gson.toJson(labelMap))
    }
    if (null != jobReq.getParams) jobHistory.setParams(BDPJettyServerHelper.gson.toJson(jobReq.getParams))
    jobHistory.setProgress(jobReq.getProgress)
    jobHistory.setStatus(jobReq.getStatus)
    jobHistory.setLog_path(jobReq.getLogPath)
    jobHistory.setError_code(jobReq.getErrorCode)
    jobHistory.setError_desc(jobReq.getErrorDesc)
    if (null != jobReq.getCreatedTime) jobHistory.setCreated_time(new Date(jobReq.getCreatedTime.getTime))
    if (null != jobReq.getUpdatedTime) jobHistory.setUpdated_time(new Date(jobReq.getUpdatedTime.getTime))
    jobHistory.setInstances(jobReq.getInstances)
    if (null != jobReq.getMetrics) jobHistory.setMetrics(BDPJettyServerHelper.gson.toJson(jobReq.getMetrics))
    val engineType = LabelUtil.getEngineType(jobReq.getLabels)
    jobHistory.setEngine_type(engineType)
    jobHistory.setExecution_code(jobReq.getExecutionCode)
    jobHistory
  }


  def subjobDetails2JobDetail(subjobDetails: util.List[SubJobDetail]): util.List[JobDetail] = {
    val lists = new util.ArrayList[JobDetail](subjobDetails.size())
    subjobDetails.asScala.foreach(j => lists.add(subjobDetail2JobDetail(j)))
    lists
  }

  def jobdetails2SubjobDetail(jobdetails: util.List[JobDetail]): util.List[SubJobDetail] = {
    val lists = new util.ArrayList[SubJobDetail](jobdetails.size())
    jobdetails.asScala.foreach(j => lists.add(jobdetail2SubjobDetail(j)))
    lists
  }

  def subjobDetail2JobDetail(subjob: SubJobDetail): JobDetail = {
    if (null == subjob) return null
    val jobDetail = new JobDetail
    jobDetail.setId(subjob.getId)
    jobDetail.setJob_history_id(subjob.getJobGroupId)
    jobDetail.setResult_location(subjob.getResultLocation)
    jobDetail.setResult_array_size(subjob.getResultSize)
    jobDetail.setExecution_content(subjob.getExecutionContent)
    jobDetail.setJob_group_info(subjob.getJobGroupInfo)
    jobDetail.setCreated_time(subjob.getCreatedTime)
    jobDetail.setUpdated_time(subjob.getUpdatedTime)
    jobDetail.setStatus(subjob.getStatus)
    jobDetail.setPriority(subjob.getPriority)
    jobDetail
  }

  def jobdetail2SubjobDetail(jobdetail: JobDetail): SubJobDetail = {
    if (null == jobdetail) return null
    val subjobDetail = new SubJobDetail
    subjobDetail.setId(jobdetail.getId)
    subjobDetail.setJobGroupId(jobdetail.getJob_history_id)
    subjobDetail.setResultLocation(jobdetail.getResult_location)
    subjobDetail.setResultSize(jobdetail.getResult_array_size)
    subjobDetail.setExecutionContent(jobdetail.getExecution_content)
    subjobDetail.setJobGroupInfo(jobdetail.getJob_group_info)
    subjobDetail.setCreatedTime(jobdetail.getCreated_time)
    subjobDetail.setUpdatedTime(jobdetail.getUpdated_time)
    subjobDetail.setStatus(jobdetail.getStatus)
    subjobDetail.setPriority(jobdetail.getPriority)
    subjobDetail
  }

  def jobHistory2TaskVO(job: JobHistory, subjobs: util.List[SubJobDetail]): QueryTaskVO = {
    if (null == job) return null
    val taskVO = new QueryTaskVO
    taskVO.setTaskID(job.getId)
    taskVO.setInstance(job.getInstances)
    taskVO.setExecId(job.getJob_req_id)
    taskVO.setUmUser(job.getSubmit_user)
    taskVO.setEngineInstance(null)
    taskVO.setProgress(job.getProgress)
    taskVO.setLogPath(job.getLog_path)
    taskVO.setStatus(job.getStatus)
    if (null != job.getCreated_time) taskVO.setCreatedTime(new Date(job.getCreated_time.getTime))
    if (null != job.getUpdated_time) taskVO.setUpdatedTime(new Date(job.getUpdated_time.getTime))
    val labelList = getLabelListFromJson(job.getLabels)
    var engineType = job.getEngine_type
    var codeType = ""
    var creator = ""
    if (null != labelList && labelList.size() > 0) {
      if (null == engineType) {
        engineType = LabelUtil.getEngineType(labelList)
      }
      codeType = LabelUtil.getCodeType(labelList)
      val userCreator = Option(LabelUtil.getUserCreator(labelList)).getOrElse(null)
      if (null != userCreator) {
        creator = userCreator._2
      }
    }
    taskVO.setEngineType(engineType)
    taskVO.setExecuteApplicationName(job.getEngine_type)
    taskVO.setRequestApplicationName(creator)
    taskVO.setRunType(codeType)
    taskVO.setParamsJson(job.getParams)
    taskVO.setCreatedTime(job.getCreated_time)
    taskVO.setUpdatedTime(job.getUpdated_time)
    taskVO.setErrCode(job.getError_code)
    taskVO.setErrDesc(job.getError_desc)

    val metrics = BDPJettyServerHelper.gson.fromJson((job.getMetrics), classOf[util.Map[String, Object]])
    var completeTime: Date = null
    if(null != metrics && metrics.containsKey(TaskConstant.ENTRANCEJOB_COMPLETE_TIME) && metrics.get(TaskConstant.ENTRANCEJOB_COMPLETE_TIME) != null){
      completeTime = dealString2Date(metrics.get(TaskConstant.ENTRANCEJOB_COMPLETE_TIME).toString)
    }
    var createTime: Date = null
    if(null != metrics && metrics.containsKey(TaskConstant.ENTRANCEJOB_SUBMIT_TIME) && metrics.get(TaskConstant.ENTRANCEJOB_SUBMIT_TIME) != null){
      createTime = dealString2Date(metrics.get(TaskConstant.ENTRANCEJOB_SUBMIT_TIME).toString)
    }
    if(isJobFinished(job.getStatus) && null != completeTime && null != createTime){
      taskVO.setCostTime(completeTime.getTime - createTime.getTime)
    }else if (null != createTime){
      taskVO.setCostTime(System.currentTimeMillis() - createTime.getTime)
    }

    val entranceName = JobhistoryConfiguration.ENTRANCE_SPRING_NAME.getValue
    val instances = job.getInstances().split(JobhistoryConfiguration.ENTRANCE_INSTANCE_DELEMITER.getValue)
    taskVO.setStrongerExecId(ZuulEntranceUtils.generateExecID(job.getJob_req_id, entranceName, instances))
    taskVO.setSourceJson(job.getSource)
    if (StringUtils.isNotBlank(job.getExecution_code)) {
          taskVO.setExecutionCode(job.getExecution_code)
        }
        // Do not attach subjobs for performance
    //    taskVO.setSubJobs(subjobs)
    taskVO.setSourceJson(job.getSource)
    if (StringUtils.isNotBlank(job.getSource)) {
      Utils.tryCatch {
        val source = BDPJettyServerHelper.gson.fromJson(job.getSource, classOf[util.Map[String, String]])
        taskVO.setSourceTailor(source.map(_._2).foldLeft("")(_ + _ + "-").stripSuffix("-"))
      } {
        case _ => warn("sourceJson deserialization failed, this task may be the old data.")
      }
    }
    taskVO
  }

  def getLabelListFromJson(labelJson: String): util.List[Label[_]] = {
    val labelMap = BDPJettyServerHelper.gson.fromJson(labelJson, classOf[util.HashMap[String, String]])
    val labels = new util.ArrayList[Label[_]]()
    Utils.tryAndWarn {
      labelMap.asScala.map(l => labelBuilderFactory.createLabel[Label[_]](l._1, l._2)).foreach(labels.add)
    }
    labels
  }

  def dealString2Date(strDate : String) : Date = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    Utils.tryCatch{
      val date = df.parse(strDate)
      date
    } {
      _ => warn("String to Date deserialization failed.")
        null
    }
  }
}
