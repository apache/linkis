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

package org.apache.linkis.jobhistory.service.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.entity.job.{
  JobRequest,
  JobRequestWithDetail,
  QueryException,
  SubJobDetail
}
import org.apache.linkis.governance.common.protocol.conf.EntranceInstanceConfRequest
import org.apache.linkis.governance.common.protocol.job._
import org.apache.linkis.jobhistory.conversions.TaskConversions._
import org.apache.linkis.jobhistory.dao.JobHistoryMapper
import org.apache.linkis.jobhistory.entity.{JobHistory, QueryJobHistory}
import org.apache.linkis.jobhistory.errorcode.JobhistoryErrorCodeSummary
import org.apache.linkis.jobhistory.service.JobHistoryQueryService
import org.apache.linkis.jobhistory.transitional.TaskStatus
import org.apache.linkis.jobhistory.util.QueryUtils
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.lang3.time.DateUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.{lang, util}
import java.sql.Timestamp
import java.util.{Calendar, Date}
import java.util.concurrent.{Callable, TimeUnit}

import scala.collection.JavaConverters._

import com.google.common.cache.{Cache, CacheBuilder}
import com.google.common.collect.{Iterables, Lists}

@Service
class JobHistoryQueryServiceImpl extends JobHistoryQueryService with Logging {

  @Autowired
  private var jobHistoryMapper: JobHistoryMapper = _

  private val unDoneTaskCache: Cache[String, Integer] = CacheBuilder
    .newBuilder()
    .concurrencyLevel(5)
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .initialCapacity(20)
    .maximumSize(1000)
    .recordStats()
    .build()

  @Receiver
  override def add(jobReqInsert: JobReqInsert): JobRespProtocol = {
    logger.info("Insert data into the database(往数据库中插入数据)：" + jobReqInsert.toString)
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      QueryUtils.storeExecutionCode(jobReqInsert.jobReq)
      val jobInsert = jobRequest2JobHistory(jobReqInsert.jobReq)
      jobInsert.setUpdatedTime(jobInsert.getCreatedTime)
      jobHistoryMapper.insertJobHistory(jobInsert)
      val map = new util.HashMap[String, Object]()
      map.put(JobRequestConstants.JOB_ID, jobInsert.getId.asInstanceOf[Object])
      jobResp.setStatus(0)
      jobResp.setData(map)
    } { case exception: Exception =>
      logger.error(
        s"Failed to add JobReqInsert ${jobReqInsert.toString},should be retry",
        exception
      )
      jobResp.setStatus(2)
      jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
    }
    jobResp
  }

  @Receiver
  override def change(jobReqUpdate: JobReqUpdate): JobRespProtocol = {
    val jobReq = jobReqUpdate.jobReq
    jobReq.setExecutionCode(null)
    logger.info(
      "Update data to the database(往数据库中更新数据)：task " + jobReq.getId + "status:" + jobReq.getStatus
    )
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      if (jobReq.getErrorDesc != null) {
        if (jobReq.getErrorDesc.length > GovernanceCommonConf.ERROR_CODE_DESC_LEN) {
          logger.info(s"errorDesc is too long,we will cut some message")
          jobReq.setErrorDesc(
            jobReq.getErrorDesc.substring(0, GovernanceCommonConf.ERROR_CODE_DESC_LEN - 3) + "..."
          )
          logger.info(s"${jobReq.getErrorDesc}")
        }
      }
      if (jobReq.getStatus != null) {
        val oldStatus: String = jobHistoryMapper.selectJobHistoryStatusForUpdate(jobReq.getId)
        if (oldStatus != null && !shouldUpdate(oldStatus, jobReq.getStatus)) {
          throw new QueryException(
            120001,
            s"jobId:${jobReq.getId}，oldStatus(在数据库中的task状态为)：${oldStatus}," +
              s" newStatus(更新的task状态为)：${jobReq.getStatus}，update failed(更新失败)！"
          )
        }
      }
      val jobUpdate = jobRequest2JobHistory(jobReq)
      if (jobUpdate.getUpdatedTime == null) {
        throw new QueryException(
          120001,
          s"jobId:${jobReq.getId}，update job failed, updatetime needed(更新job相关信息失败，请指定该请求的更新时间)!"
        )
      }
      logger.info(
        s"Update data to the database(往数据库中更新数据)：task ${jobReq.getId} ,status ${jobReq.getStatus}," +
          s" updateTime: ${jobUpdate.getUpdateTimeMills}, progress : ${jobUpdate.getProgress}"
      )
      jobHistoryMapper.updateJobHistory(jobUpdate)
      val map = new util.HashMap[String, Object]
      map.put(JobRequestConstants.JOB_ID, jobReq.getId.asInstanceOf[Object])
      jobResp.setStatus(0)
      jobResp.setData(map)
    } {
      case exception: QueryException =>
        logger.error(
          s"Failed to update JobReqUpdate ${jobReq.getId},status ${jobReq.getStatus}",
          exception
        )
        jobResp.setStatus(1)
        jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
      case exception: Exception =>
        logger.error(
          s"Failed to update JobReqUpdate ${jobReq.getId},status ${jobReq.getStatus}",
          exception
        )
        jobResp.setStatus(2)
        jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
    }
    jobResp
  }

  @Receiver
  override def batchChange(jobReqUpdate: JobReqBatchUpdate): util.ArrayList[JobRespProtocol] = {
    val jobReqList = jobReqUpdate.jobReq
    val jobRespList = new util.ArrayList[JobRespProtocol]()
    if (jobReqList != null) {
      jobReqList.asScala.foreach(jobReq => {
        jobReq.setExecutionCode(null)
        logger.info("Update data to the database(往数据库中更新数据)：status:" + jobReq.getStatus)
        val jobResp = new JobRespProtocol
        Utils.tryCatch {
          if (jobReq.getErrorDesc != null) {
            if (jobReq.getErrorDesc.length > GovernanceCommonConf.ERROR_CODE_DESC_LEN) {
              logger.info(s"errorDesc is too long,we will cut some message")
              jobReq.setErrorDesc(
                jobReq.getErrorDesc
                  .substring(0, GovernanceCommonConf.ERROR_CODE_DESC_LEN - 3) + "..."
              )
              logger.info(s"${jobReq.getErrorDesc}")
            }
          }
          if (jobReq.getStatus != null) {
            val oldStatus: String = jobHistoryMapper.selectJobHistoryStatusForUpdate(jobReq.getId)
            if (oldStatus != null && !shouldUpdate(oldStatus, jobReq.getStatus)) {
              throw new QueryException(
                120001,
                s"jobId:${jobReq.getId}，oldStatus(在数据库中的task状态为)：${oldStatus}，" +
                  s"newStatus(更新的task状态为)：${jobReq.getStatus}，update failed(更新失败)！"
              )
            }
          }
          val jobUpdate = jobRequest2JobHistory(jobReq)
          jobUpdate.setUpdatedTime(new Timestamp(System.currentTimeMillis()))
          jobHistoryMapper.updateJobHistory(jobUpdate)

          // todo
          /* //to write cache
            if (TaskStatus.Succeed.toString.equals(jobReq.getStatus) && queryCacheService.needCache(jobReq)) {
              info("Write cache for task: " + jobReq.getId)
              jobReq.setExecutionCode(executionCode)
              queryCacheService.writeCache(jobReq)
            } */

          val map = new util.HashMap[String, Object]
          map.put(JobRequestConstants.JOB_ID, jobReq.getId.asInstanceOf[Object])
          jobResp.setStatus(0)
          jobResp.setData(map)
        } { case e: Exception =>
          logger.error(
            s"Failed to update JobReqUpdate ${jobReq.getId},status ${jobReq.getStatus}",
            e
          )
          jobResp.setStatus(1)
          jobResp.setMsg(ExceptionUtils.getRootCauseMessage(e))
        }
        jobRespList.add(jobResp)
      })
    }
    jobRespList
  }

  @Receiver
  override def query(jobReqQuery: JobReqQuery): JobRespProtocol = {
    logger.info("query history task：" + jobReqQuery.toString)
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      val jobHistory = jobRequest2JobHistory(jobReqQuery.jobReq)
      val task = jobHistoryMapper.selectJobHistory(jobHistory)
      val tasksWithDetails = new util.ArrayList[JobRequestWithDetail]
      task.asScala.foreach(job => {
        val subJobDetails = new util.ArrayList[SubJobDetail]()
        val subJobDetail = new SubJobDetail
        subJobDetail.setResultLocation(job.getResultLocation)
        subJobDetails.add(subJobDetail)
        tasksWithDetails.add(
          new JobRequestWithDetail(jobHistory2JobRequest(job)).setSubJobDetailList(subJobDetails)
        )
      })
      val map = new util.HashMap[String, Object]()
      map.put(JobRequestConstants.JOB_HISTORY_LIST, tasksWithDetails)
      jobResp.setStatus(0)
      jobResp.setData(map)
    } { case e: Exception =>
      logger.error(s"Failed to query job ${jobReqQuery.jobReq.getId}", e)
      jobResp.setStatus(1)
      jobResp.setMsg(ExceptionUtils.getRootCauseMessage(e))
    }
    jobResp
  }

  override def getJobHistoryByIdAndName(jobId: java.lang.Long, userName: String): JobHistory = {
    val jobReq = new JobHistory
    jobReq.setId(jobId)
    jobReq.setSubmitUser(userName)
    val jobHistoryList = jobHistoryMapper.selectJobHistory(jobReq)
    if (jobHistoryList.isEmpty) null else jobHistoryList.get(0)
  }

  override def search(
      jobId: lang.Long,
      username: String,
      status: String,
      creator: String,
      sDate: Date,
      eDate: Date,
      engineType: String,
      startJobId: lang.Long,
      instance: String
  ): util.List[JobHistory] = {

    val split: util.List[String] = if (status != null) status.split(",").toList.asJava else null
    val result = if (StringUtils.isBlank(creator)) {
      jobHistoryMapper.search(
        jobId,
        username,
        split,
        sDate,
        eDate,
        engineType,
        startJobId,
        instance
      )
    } else if (StringUtils.isBlank(username)) {
      val fakeLabel = new UserCreatorLabel
      jobHistoryMapper.searchWithCreatorOnly(
        jobId,
        username,
        fakeLabel.getLabelKey,
        creator,
        split,
        sDate,
        eDate,
        engineType,
        startJobId,
        instance
      )
    } else {
      val fakeLabel = new UserCreatorLabel
      fakeLabel.setUser(username)
      fakeLabel.setCreator(creator)
      val userCreator = fakeLabel.getStringValue
      Utils.tryCatch(fakeLabel.valueCheck(userCreator)) { t =>
        logger.info("input user or creator is not correct", t)
        throw t
      }
      jobHistoryMapper.searchWithUserCreator(
        jobId,
        username,
        fakeLabel.getLabelKey,
        userCreator,
        split,
        sDate,
        eDate,
        engineType,
        startJobId,
        instance
      )
    }
    result
  }

  override def getQueryVOList(list: java.util.List[JobHistory]): java.util.List[JobRequest] = {
    jobHistory2JobRequest(list)
  }

  private def shouldUpdate(oldStatus: String, newStatus: String): Boolean = {
    if (TaskStatus.valueOf(oldStatus) == TaskStatus.valueOf(newStatus)) {
      true
    } else {
      TaskStatus.valueOf(oldStatus).ordinal <= TaskStatus
        .valueOf(newStatus)
        .ordinal && !TaskStatus.isComplete(TaskStatus.valueOf(oldStatus))
    }
  }

  override def searchOne(jobId: lang.Long, sDate: Date, eDate: Date): JobHistory = {
    Iterables.getFirst(
      jobHistoryMapper.search(jobId, null, null, sDate, eDate, null, null, null), {
        val queryJobHistory = new QueryJobHistory
        queryJobHistory.setId(jobId)
        queryJobHistory.setStatus(TaskStatus.Inited.toString)
        queryJobHistory.setSubmitUser("EMPTY")
        queryJobHistory
      }
    )
  }

  override def countUndoneTasks(
      username: String,
      creator: String,
      sDate: Date,
      eDate: Date,
      engineType: String,
      startJobId: lang.Long
  ): Integer = {
    val cacheKey =
      if (StringUtils.isNoneBlank(username, creator, engineType)) ""
      else {
        s"${username}_${creator}_${engineType}"
      }
    if (StringUtils.isBlank(cacheKey)) {
      getCountUndoneTasks(username, creator, sDate, eDate, engineType, startJobId)
    } else {
      unDoneTaskCache.get(
        cacheKey,
        new Callable[Integer] {
          override def call(): Integer = {
            getCountUndoneTasks(username, creator, sDate, eDate, engineType, startJobId)
          }
        }
      )
    }
  }

  private def getCountUndoneTasks(
      username: String,
      creator: String,
      sDate: Date,
      eDate: Date,
      engineType: String,
      startJobId: lang.Long
  ): Integer = {
    val statusList: util.List[String] = new util.ArrayList[String]()
    statusList.add(TaskStatus.Running.toString)
    statusList.add(TaskStatus.Inited.toString)
    statusList.add(TaskStatus.Scheduled.toString)

    val count = if (StringUtils.isBlank(creator)) {
      jobHistoryMapper.countUndoneTaskNoCreator(
        username,
        statusList,
        sDate,
        eDate,
        engineType,
        startJobId
      )
    } else if (StringUtils.isBlank(username)) {
      val fakeLabel = new UserCreatorLabel
      jobHistoryMapper.countUndoneTaskWithCreatorOnly(
        username,
        fakeLabel.getLabelKey,
        creator,
        statusList,
        sDate,
        eDate,
        engineType,
        startJobId
      )
    } else {
      val fakeLabel = new UserCreatorLabel
      fakeLabel.setUser(username)
      fakeLabel.setCreator(creator)
      val userCreator = fakeLabel.getStringValue
      Utils.tryCatch(fakeLabel.valueCheck(userCreator)) { t =>
        logger.info("input user or creator is not correct", t)
        throw t
      }
      jobHistoryMapper.countUndoneTaskWithUserCreator(
        username,
        fakeLabel.getLabelKey,
        userCreator,
        statusList,
        sDate,
        eDate,
        engineType,
        startJobId
      )
    }
    count
  }

  override def changeObserveInfoById(jobHistory: JobHistory): Unit = {
    jobHistoryMapper.updateOberverById(jobHistory.getId, jobHistory.getObserveInfo)
  }

  @Receiver
  override def clearUndoneTasksByEntranceInstance(
      request: EntranceInstanceConfRequest,
      sender: Sender
  ): Unit = {
    // Query incomplete tasks
    logger.info("Request Entrance Instance :{}", request.instance)
    val statusList: util.List[String] = new util.ArrayList[String]()
    statusList.add(TaskStatus.WaitForRetry.toString)
    statusList.add(TaskStatus.Inited.toString)
    statusList.add(TaskStatus.Scheduled.toString)
    statusList.add(TaskStatus.Running.toString)
    val eDate = new Date(System.currentTimeMillis)
    val sDate = DateUtils.addDays(eDate, -1)
    val jobHistoryList =
      jobHistoryMapper.search(null, null, statusList, sDate, eDate, null, null, request.instance)
    val idlist = jobHistoryList.asScala.map(_.getId).asJava
    logger.info("Tasks id will be canceled ids :{}", idlist)
    // Modify task status
    val errorMsg = JobhistoryErrorCodeSummary.UNFINISHED_TASKS.getErrorDesc
    if (!idlist.isEmpty) {
      if (idlist.size() >= 1000) logger.error("The number of batch modification tasks exceeds 1000")
      Lists
        .partition(idlist, 100)
        .asScala
        .foreach(idlist => jobHistoryMapper.updateJobHistoryCancelById(idlist, errorMsg))
    }
  }

}
