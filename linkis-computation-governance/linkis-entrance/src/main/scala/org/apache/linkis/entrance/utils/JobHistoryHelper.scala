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

package org.apache.linkis.entrance.utils

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.JobHistoryFailedException
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.job._
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.query.cache.{CacheTaskResult, RequestReadCache}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.SchedulerEventState

import org.apache.commons.lang3.StringUtils

import javax.servlet.http.HttpServletRequest

import java.util
import java.util.Date

import scala.collection.JavaConverters._

import com.google.common.net.InetAddresses

object JobHistoryHelper extends Logging {

  private val sender =
    Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue)

  private val SUCCESS_FLAG = 0

  def getCache(
      executionCode: String,
      user: String,
      labelStrList: util.List[String],
      readCacheBefore: Long
  ): CacheTaskResult = {
    val requestReadCache =
      new RequestReadCache(executionCode, user, labelStrList, readCacheBefore)
    sender.ask(requestReadCache) match {
      case c: CacheTaskResult => c
      case _ => null
    }
  }

  def getStatusByTaskID(taskID: Long): String = {
    val task = getTaskByTaskID(taskID)
    if (task == null) SchedulerEventState.Cancelled.toString
    else task.getStatus
  }

  def getRequestIpAddr(req: HttpServletRequest): String = {
    val addrList = List(
      Option(req.getHeader("x-forwarded-for")).getOrElse("").split(",")(0),
      Option(req.getHeader("Proxy-Client-IP")).getOrElse(""),
      Option(req.getHeader("WL-Proxy-Client-IP")).getOrElse(""),
      Option(req.getHeader("HTTP_CLIENT_IP")).getOrElse(""),
      Option(req.getHeader("HTTP_X_FORWARDED_FOR")).getOrElse("")
    )
    val afterProxyIp = addrList
      .find(ip => {
        StringUtils.isNotEmpty(ip) && InetAddresses.isInetAddress(ip)
      })
      .getOrElse("")
    if (StringUtils.isNotEmpty(afterProxyIp)) {
      afterProxyIp
    } else {
      req.getRemoteAddr
    }
  }

  /**
   * If the task cannot be found in memory, you can directly kill it
   *
   * @param taskID
   */
  def forceKill(taskID: Long): Unit = {
    val jobRequest = new JobRequest
    jobRequest.setId(taskID)
    jobRequest.setStatus(SchedulerEventState.Cancelled.toString)
    jobRequest.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS.toString)
    jobRequest.setUpdatedTime(new Date(System.currentTimeMillis()))
    val jobReqUpdate = JobReqUpdate(jobRequest)
    sender.ask(jobReqUpdate)
  }

  /**
   * Batch forced kill
   *
   * @param taskIdList
   */
  def forceBatchKill(taskIdList: util.ArrayList[java.lang.Long]): Unit = {
    val jobReqList = new util.ArrayList[JobRequest]()
    taskIdList.asScala.foreach(taskID => {
      val jobRequest = new JobRequest
      jobRequest.setId(taskID)
      jobRequest.setStatus(SchedulerEventState.Cancelled.toString)
      jobRequest.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS.toString)
      jobRequest.setUpdatedTime(new Date(System.currentTimeMillis()))
      jobReqList.add(jobRequest)
    })
    val jobReqBatchUpdate = JobReqBatchUpdate(jobReqList)
    sender.ask(jobReqBatchUpdate)
  }

  private def getTaskByTaskID(taskID: Long): JobRequest = {
    val jobRequest = new JobRequest
    jobRequest.setId(taskID)
    jobRequest.setSource(null)
    val jobReqQuery = JobReqQuery(jobRequest)
    val task = Utils.tryCatch {
      val taskResponse = sender.ask(jobReqQuery)
      taskResponse match {
        case responsePersist: JobRespProtocol =>
          val status = responsePersist.getStatus
          if (status != SUCCESS_FLAG) {
            logger.error(s"query from jobHistory status failed, status is $status")
            throw JobHistoryFailedException("query from jobHistory status failed")
          } else {
            val data = responsePersist.getData
            data.get(JobRequestConstants.JOB_HISTORY_LIST) match {
              case tasks: util.List[JobRequest] =>
                if (tasks.size() > 0) tasks.get(0)
                else null
              case _ =>
                throw JobHistoryFailedException(
                  s"query from jobhistory not a correct List type taskId is $taskID"
                )
            }
          }
        case _ =>
          logger.error("get query response incorrectly")
          throw JobHistoryFailedException("get query response incorrectly")
      }
    } {
      case errorException: ErrorException => throw errorException
      case e: Exception =>
        val e1 = JobHistoryFailedException(s"query taskId $taskID error")
        e1.initCause(e)
        throw e
    }
    task
  }

  def updateJobRequestMetrics(
      jobRequest: JobRequest,
      resourceInfo: util.Map[String, ResourceWithStatus],
      ecInfo: util.Map[String, AnyRef]
  ): Unit = {
    // update resource
    if (jobRequest.getMetrics == null) {
      jobRequest.setMetrics(new util.HashMap[String, AnyRef]())
    }
    val metricsMap = jobRequest.getMetrics
    val resourceMap = metricsMap.get(TaskConstant.ENTRANCEJOB_YARNRESOURCE)
    val ecResourceMap =
      if (resourceInfo == null) new util.HashMap[String, ResourceWithStatus] else resourceInfo
    if (resourceMap != null) {
      resourceMap.asInstanceOf[util.HashMap[String, ResourceWithStatus]].putAll(ecResourceMap)
    } else {
      metricsMap.put(TaskConstant.ENTRANCEJOB_YARNRESOURCE, ecResourceMap)
    }
    var engineInstanceMap: util.HashMap[String, AnyRef] = null
    if (metricsMap.containsKey(TaskConstant.ENTRANCEJOB_ENGINECONN_MAP)) {
      engineInstanceMap = metricsMap
        .get(TaskConstant.ENTRANCEJOB_ENGINECONN_MAP)
        .asInstanceOf[util.HashMap[String, AnyRef]]
    } else {
      engineInstanceMap = new util.HashMap[String, AnyRef]()
      metricsMap.put(TaskConstant.ENTRANCEJOB_ENGINECONN_MAP, engineInstanceMap)
    }
    val infoMap = ecInfo
    if (null != infoMap && infoMap.containsKey(TaskConstant.TICKET_ID)) {
      val ticketId = infoMap.get(TaskConstant.TICKET_ID).asInstanceOf[String]
      val engineExtraInfoMap = engineInstanceMap
        .getOrDefault(ticketId, new util.HashMap[String, AnyRef])
        .asInstanceOf[util.HashMap[String, AnyRef]]
      engineExtraInfoMap.putAll(infoMap)
      engineInstanceMap.put(ticketId, engineExtraInfoMap)
    } else {
      logger.warn("Ec info map must contains ticketID")
    }
  }

}
