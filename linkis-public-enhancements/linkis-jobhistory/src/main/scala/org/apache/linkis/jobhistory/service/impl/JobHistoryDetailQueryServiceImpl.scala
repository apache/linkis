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
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.entity.job.QueryException
import org.apache.linkis.governance.common.protocol.job._
import org.apache.linkis.jobhistory.conversions.TaskConversions._
import org.apache.linkis.jobhistory.dao.{JobDetailMapper, JobHistoryMapper}
import org.apache.linkis.jobhistory.entity.JobDetail
import org.apache.linkis.jobhistory.service.JobHistoryDetailQueryService
import org.apache.linkis.jobhistory.transitional.TaskStatus
import org.apache.linkis.jobhistory.util.QueryUtils
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util

import scala.collection.JavaConverters._
import scala.collection.JavaConverters.asScalaBufferConverter

class JobHistoryDetailQueryServiceImpl extends JobHistoryDetailQueryService with Logging {

  @Autowired
  private var jobDetailMapper: JobDetailMapper = _

  @Autowired
  private var jobHistoryMapper: JobHistoryMapper = _
//  @Autowired
//  private var queryCacheService: QueryCacheService = _

  @Receiver
  override def add(jobReqInsert: JobDetailReqInsert): JobRespProtocol = {
    val jobResp = new JobRespProtocol
    val jobReqId = jobReqInsert.jobInfo.getJobReq.getId.toString
    logger.info(s"Insert JobDetailReqInsert into the database(往数据库中插入数据):job id: $jobReqId")
    Utils.tryCatch {
      QueryUtils.storeExecutionCode(
        jobReqInsert.jobInfo.getSubJobDetail,
        jobReqInsert.jobInfo.getJobReq.getExecuteUser
      )
      val jobInsert = subjobDetail2JobDetail(jobReqInsert.jobInfo.getSubJobDetail)
      jobInsert.setUpdatedTime(jobInsert.getCreatedTime)
      jobDetailMapper.insertJobDetail(jobInsert)
      val map = new util.HashMap[String, Object]()
      map.put(JobRequestConstants.JOB_ID, jobInsert.getId.asInstanceOf[Object])
      jobResp.setStatus(0)
      jobResp.setData(map)
    } { case e: Exception =>
      logger.error(s"Failed to add JobDetailReqInsert ${jobReqId}", e)
      jobResp.setStatus(1)
      jobResp.setMsg(ExceptionUtils.getRootCauseMessage(e))
    }
    jobResp
  }

  @Receiver
  @Transactional
  override def change(jobReqUpdate: JobDetailReqUpdate): JobRespProtocol = {
    val subJobInfo = jobReqUpdate.jobInfo
    val jobDetail = subJobInfo.getSubJobDetail()
    if (null != jobDetail && null != jobDetail.getId) {
      logger.info("Update data to the database(往数据库中更新数据)：" + jobDetail.getId.toString)
    }
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      if (jobDetail.getStatus != null) {
        val oldStatus: String =
          jobDetailMapper.selectJobDetailStatusForUpdateByJobDetailId(jobDetail.getId)
        if (oldStatus != null && !shouldUpdate(oldStatus, jobDetail.getStatus)) {
          throw new QueryException(
            120001,
            s"${jobDetail.getId}数据库中的task状态为：${oldStatus}更新的task状态为：${jobDetail.getStatus}更新失败！"
          )
        }
      }
      jobDetail.setExecutionContent(null)
      val jobUpdate = subjobDetail2JobDetail(jobDetail)
      if (jobUpdate.getUpdatedTime == null) {
        throw new QueryException(120001, s"job${jobUpdate.getId}更新job相关信息失败，请指定该请求的更新时间!")
      }
      jobDetailMapper.updateJobDetail(jobUpdate)

      // todo
      /* // to write cache
      if (TaskStatus.Succeed.toString.equals(jobReq.getStatus) && queryCacheService.needCache(jobReq)) {
        info("Write cache for task: " + jobReq.getId)
        jobReq.setExecutionCode(executionCode)
        queryCacheService.writeCache(jobReq)
      } */

      val map = new util.HashMap[String, Object]
      map.put(JobRequestConstants.JOB_ID, jobDetail.getId.asInstanceOf[Object])
      jobResp.setStatus(0)
      jobResp.setData(map)
    } {
      case exception: QueryException =>
        logger.error(s"Failed to change JobDetailReqInsert ${jobDetail.getId}", exception)
        jobResp.setStatus(1)
        jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
      case exception: Exception =>
        logger.error(
          s"Failed to change JobDetailReqInsert ${jobDetail.getId}, should be retry",
          exception
        )
        jobResp.setStatus(2)
        jobResp.setMsg(ExceptionUtils.getRootCauseMessage(exception))
    }
    jobResp
  }

  @Receiver
  @Transactional
  override def batchChange(
      jobReqUpdate: JobDetailReqBatchUpdate
  ): util.ArrayList[JobRespProtocol] = {
    val subJobInfoList = jobReqUpdate.jobInfo
    val jobRespList = new util.ArrayList[JobRespProtocol]()
    if (subJobInfoList != null) {
      subJobInfoList.asScala.foreach(subJobInfo => {
        val jobDetail = subJobInfo.getSubJobDetail()
        if (null != jobDetail && null != jobDetail.getId) {
          logger.info("Update data to the database(往数据库中更新数据)：" + jobDetail.getId.toString)
        }
        val jobResp = new JobRespProtocol
        Utils.tryCatch {
          if (jobDetail.getStatus != null) {
            val oldStatus: String =
              jobDetailMapper.selectJobDetailStatusForUpdateByJobDetailId(jobDetail.getId)
            if (oldStatus != null && !shouldUpdate(oldStatus, jobDetail.getStatus)) {
              throw new QueryException(
                120001,
                s"${jobDetail.getId}数据库中的task状态为：${oldStatus}更新的task状态为：${jobDetail.getStatus}更新失败！"
              )
            }
          }
          jobDetail.setExecutionContent(null)
          val jobUpdate = subjobDetail2JobDetail(jobDetail)
          if (jobUpdate.getUpdatedTime == null) {
            throw new QueryException(120001, s"job${jobUpdate.getId}更新job相关信息失败，请指定该请求的更新时间!")
          }
          jobDetailMapper.updateJobDetail(jobUpdate)

          // todo
          /* //to write cache
            if (TaskStatus.Succeed.toString.equals(jobReq.getStatus) && queryCacheService.needCache(jobReq)) {
              info("Write cache for task: " + jobReq.getId)
              jobReq.setExecutionCode(executionCode)
              queryCacheService.writeCache(jobReq)
            } */

          val map = new util.HashMap[String, Object]
          map.put(JobRequestConstants.JOB_ID, jobDetail.getId.asInstanceOf[Object])
          jobResp.setStatus(0)
          jobResp.setData(map)
        } { case e: Exception =>
          logger.error(s"Failed to abatchChange", e)
          jobResp.setStatus(1)
          jobResp.setMsg(ExceptionUtils.getRootCauseMessage(e))
        }
        jobRespList.add(jobResp)
      })
    }
    jobRespList
  }

  @Receiver
  override def query(jobReqQuery: JobDetailReqQuery): JobRespProtocol = {
    logger.info("查询历史task：" + jobReqQuery.toString)
    val jobResp = new JobRespProtocol
    Utils.tryCatch {
      val subjobDetail = subjobDetail2JobDetail(jobReqQuery.jobReq)
      val details = jobDetailMapper.queryJobHistoryDetail(subjobDetail)
      val map = new util.HashMap[String, Object]()
      val detailList = new util.ArrayList[JobDetail]()
      details.asScala.map(_.asInstanceOf[JobDetail]).foreach(d => detailList.add(d))
      map.put(JobRequestConstants.JOB_DETAIL_LIST, jobdetails2SubjobDetail(detailList))
      jobResp.setStatus(0)
      jobResp.setData(map)
    } { case e: Exception =>
      logger.error(s"Failed to query history task", e)
      jobResp.setStatus(1)
      jobResp.setMsg(e.getMessage);
    }
    jobResp
  }

  private def shouldUpdate(oldStatus: String, newStatus: String): Boolean =
    TaskStatus.valueOf(oldStatus).ordinal <= TaskStatus.valueOf(newStatus).ordinal

}
