package com.webank.wedatasphere.linkis.entrance.utils

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.JobHistoryFailedException
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.governance.common.constant.job.JobRequestConstants
import com.webank.wedatasphere.linkis.governance.common.entity.job.{JobRequest, SubJobDetail, SubJobInfo}
import com.webank.wedatasphere.linkis.governance.common.protocol.job._
import com.webank.wedatasphere.linkis.protocol.query.cache.{CacheTaskResult, RequestReadCache}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import java.util

import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang.StringUtils
import sun.net.util.IPAddressUtil

import scala.collection.JavaConversions._

object JobHistoryHelper extends Logging{

  private val sender = Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue)

  private val SUCCESS_FLAG = 0

  def getCache(executionCode: String, user: String, labelStrList: util.List[String], readCacheBefore: Long): CacheTaskResult ={
    val requestReadCache = new RequestReadCache(executionCode, user, labelStrList, readCacheBefore)
    sender.ask(requestReadCache) match {
      case  c: CacheTaskResult => c
      case _ => null
    }
  }

  def getStatusByTaskID(taskID:Long):String = {
    val task = getTaskByTaskID(taskID)
    if (task == null) SchedulerEventState.Cancelled.toString
    else task.getStatus
  }

  def getRequestIpAddr(req: HttpServletRequest ): String = {
    val addrList = List(Option(req.getHeader("x-forwarded-for")).getOrElse("").split(",")(0),
      Option(req.getHeader("Proxy-Client-IP")).getOrElse(""),
      Option(req.getHeader("WL-Proxy-Client-IP")).getOrElse(""),
      Option(req.getHeader("HTTP_CLIENT_IP")).getOrElse(""),
      Option(req.getHeader("HTTP_X_FORWARDED_FOR")).getOrElse("")
    )
    val afterProxyIp = addrList.find(ip => {StringUtils.isNotEmpty(ip) &&
      (IPAddressUtil.isIPv4LiteralAddress(ip) || IPAddressUtil.isIPv6LiteralAddress(ip))}).getOrElse("")
    if(StringUtils.isNotEmpty(afterProxyIp)){
      afterProxyIp
    }else{
      req.getRemoteAddr
    }
  }

  /**
   * 对于一个在内存中找不到这个任务的话，可以直接干掉
   * @param taskID
   */
  def forceKill(taskID:Long):Unit = {
    val subJobInfo = new SubJobInfo
    val subJobDetail = new SubJobDetail
    subJobDetail.setId(taskID)
    subJobDetail.setStatus(SchedulerEventState.Cancelled.toString)
    subJobInfo.setSubJobDetail(subJobDetail)
    val jobDetailReqUpdate = JobDetailReqUpdate(subJobInfo)
    val jobRequest = new JobRequest
    jobRequest.setId(taskID)
    jobRequest.setStatus(SchedulerEventState.Cancelled.toString)
    jobRequest.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS.toString)
    val jobReqUpdate = JobReqUpdate(jobRequest)
    sender.ask(jobReqUpdate)
    sender.ask(jobDetailReqUpdate)
  }

  /**
   * 批量强制kill
   * @param taskIdList
   */
  def forceBatchKill(taskIdList: util.ArrayList[java.lang.Long]):Unit = {
    val subJobInfoList = new util.ArrayList[SubJobInfo]()
    val jobReqList = new util.ArrayList[JobRequest]()
    taskIdList.foreach(taskID => {
      val subJobInfo = new SubJobInfo
      val subJobDetail = new SubJobDetail
      subJobDetail.setId(taskID)
      subJobDetail.setStatus(SchedulerEventState.Cancelled.toString)
      subJobInfo.setSubJobDetail(subJobDetail)
      subJobInfoList.add(subJobInfo)
      val jobRequest = new JobRequest
      jobRequest.setId(taskID)
      jobRequest.setStatus(SchedulerEventState.Cancelled.toString)
      jobRequest.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS.toString)
      jobReqList.add(jobRequest)
    })
    val jobDetailReqBatchUpdate = JobDetailReqBatchUpdate(subJobInfoList)
    val jobReqBatchUpdate = JobReqBatchUpdate(jobReqList)
    sender.ask(jobDetailReqBatchUpdate)
    sender.ask(jobReqBatchUpdate)
  }

  private def getTaskByTaskID(taskID:Long): JobRequest = {
    val jobRequest = new JobRequest
    jobRequest.setId(taskID)
    jobRequest.setSource(null)
    val jobReqQuery = JobReqQuery(jobRequest)
    val task = Utils.tryCatch{
      val taskResponse = sender.ask(jobReqQuery)
      taskResponse match {
        case responsePersist: JobRespProtocol =>
          val status = responsePersist.getStatus
          if (status != SUCCESS_FLAG){
            logger.error(s"query from jobHistory status failed, status is $status")
            throw JobHistoryFailedException("query from jobHistory status failed")
          }else{
            val data = responsePersist.getData
            data.get(JobRequestConstants.JOB_HISTORY_LIST) match {
              case tasks: util.List[JobRequest] =>
                if (tasks.size() > 0) tasks.get(0)
                else null
              case _ => throw JobHistoryFailedException(s"query from jobhistory not a correct List type taskId is $taskID")
            }
          }
        case _ => logger.error("get query response incorrectly")
          throw JobHistoryFailedException("get query response incorrectly")
      }
    }{
      case errorException:ErrorException => throw errorException
      case e:Exception => val e1 = JobHistoryFailedException(s"query taskId $taskID error")
        e1.initCause(e)
        throw e
    }
    task
  }


}
