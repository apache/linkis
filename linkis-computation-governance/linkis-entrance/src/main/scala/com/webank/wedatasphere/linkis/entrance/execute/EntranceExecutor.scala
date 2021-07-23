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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecuteRequest
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus._
import com.webank.wedatasphere.linkis.governance.common.protocol.task.{RequestTask, ResponseTaskStatus}
import com.webank.wedatasphere.linkis.orchestrator.Orchestration
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.log.LogProcessor
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress.ProgressProcessor
import com.webank.wedatasphere.linkis.orchestrator.core.OrchestrationFuture
import com.webank.wedatasphere.linkis.protocol.UserWithCreator
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer._
import org.apache.hadoop.fs.Options.CreateOpts.Progress

import scala.collection.mutable.ArrayBuffer


abstract class EntranceExecutor(val id: Long, val mark: MarkReq) extends Executor with Logging {

  private implicit var userWithCreator: UserWithCreator = _

  protected val engineReturns = ArrayBuffer[EngineExecuteAsynReturn]()

  protected var interceptors: Array[ExecuteRequestInterceptor] = Array(LabelExecuteRequestInterceptor, JobExecuteRequestInterceptor)

  def setInterceptors(interceptors: Array[ExecuteRequestInterceptor]) = if (interceptors != null && interceptors.nonEmpty) {
    this.interceptors = interceptors
  }

  def setUser(user: String): Unit = userWithCreator = if (userWithCreator != null) UserWithCreator(user, userWithCreator.creator)
  else UserWithCreator(user, null)

  def getUser = if (userWithCreator != null) userWithCreator.user else null

  def setCreator(creator: String): Unit = userWithCreator = if (userWithCreator != null) UserWithCreator(userWithCreator.user, creator)
  else UserWithCreator(null, creator)

  def getCreator = if (userWithCreator != null) userWithCreator.creator else null

//  def getInstance: ServiceInstance = getEngineConnExecutor().getServiceInstance

  private[execute] def getEngineReturns = engineReturns.toArray

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    var request: RequestTask = null
    interceptors.foreach(in => request = in.apply(request, executeRequest))
    /*if (request.getProperties != null &&
      request.getProperties.containsKey(ReconnectExecuteRequestInterceptor.PROPERTY_EXEC_ID)) {
      val execId = ReconnectExecuteRequestInterceptor.PROPERTY_EXEC_ID.toString
      Utils.tryCatch {
        val engineReturn = new EngineExecuteAsynReturn(request, null, execId, _ => callback())
        engineReturns synchronized engineReturns += engineReturn
        return engineReturn
      } { t: Throwable =>
        error(s"Failed to get execId $execId status", t)
      }
    }*/
    val engineReturn = callExecute(executeRequest)
    engineReturns synchronized engineReturns += engineReturn
    engineReturn
  }

  protected def callback(): Unit = {}

  protected def callExecute(request: ExecuteRequest): EngineExecuteAsynReturn

//  override def toString: String = s"${getInstance.getApplicationName}Engine($getId, $getUser, $getCreator, ${getInstance.getInstance})"
  override def toString: String = s"string"

  protected def killExecId(asynReturn: EngineExecuteAsynReturn, subJobId: String): Boolean = {
    info(s"begin to send killExecId, subJobId : $subJobId")
    Utils.tryCatch {
      asynReturn.orchestrationFuture.cancel(s"Job ${subJobId} was cancelled by user.")
      true
    }{
      case t : Throwable =>
        error(s"Kill subjob with id : ${subJobId} failed, ${t.getMessage}")
        false
    }
  }

  override def getId: Long = this.id

  override def state: ExecutorState = ExecutorState.Idle

  override def getExecutorInfo: ExecutorInfo = {
    null
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[EntranceExecutor]

  override def equals(other: Any): Boolean = other match {
    case that: EntranceExecutor =>
      (that canEqual this) && that.getId == this.getId
    case _ => false
  }

  def getExecId(jobId: String): String = {
    val erOption = engineReturns.find(_.getJobId.contains(jobId))
    if ( erOption.isDefined ) {
      erOption.get.subJobId
    } else {
      null
    }
  }

  override def hashCode(): Int = {
//    getOrchestratorSession().hashCode()
    // todo
    super.hashCode()
  }

  def getRunningOrchestrationFuture: Option[OrchestrationFuture] = {
    if (null != engineReturns && engineReturns.nonEmpty ) {
      Some(engineReturns.last.orchestrationFuture)
    } else {
      None
    }
  }
}

class EngineExecuteAsynReturn(val request: ExecuteRequest, val orchestrationFuture: OrchestrationFuture,
                              val subJobId: String, logProcessor: LogProcessor, progressProcessor: ProgressProcessor = null,
                              callback: EngineExecuteAsynReturn => Unit) extends AsynReturnExecuteResponse with Logging {
  getJobId.foreach(id => info("Job " + id + " received a subjobId " + subJobId + " from orchestrator"))
  private var notifyJob: ExecuteResponse => Unit = _

  private var error: Throwable = _

  private var errorMsg: String = _

  private var lastNotifyTime = System.currentTimeMillis

  def getLastNotifyTime = lastNotifyTime

  private[execute] def notifyStatus(responseEngineStatus: ResponseTaskStatus): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    val response = responseEngineStatus.status match {
      case Succeed => Some(SuccessExecuteResponse())
      case Failed | Cancelled | Timeout => Some(ErrorExecuteResponse(errorMsg, error))
      case _ => None
    }
    response.foreach { r =>
      getJobId.foreach(id => {
        var subJobId: Long = 0l
        request match {
          case entranceExecuteRequest: EntranceExecuteRequest =>
            subJobId = entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId
            val msg = "Job with execId-" + id + " and subJobId : " + subJobId  + " from orchestrator" + " completed with state " + r
            entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, msg))
          case _ =>
        }

        val msgInfo = "Job with execId-" + id + " and subJobId : " + subJobId  + " from orchestrator" + " completed with state " + r
        info(msgInfo)
        request
      })
      if (null != logProcessor) {
        logProcessor.close()
      }
      if (null != progressProcessor){
        progressProcessor.close()
      }
      Utils.tryAndWarn(if(null != callback) {
        callback(this)
      })
      if (null != notifyJob) {
        notifyJob
      }
      else {
        info("NotifyJob is null.")
      }
//      if (notifyJob == null) this synchronized (while (notifyJob == null) this.wait(1000))
//      if (null != notifyJob) notifyJob(r)
    }
  }

  private[execute] def notifyHeartbeat(): Unit = {
    lastNotifyTime = System.currentTimeMillis()
  }

  private[execute] def notifyError(errorMsg: String): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    this.errorMsg = errorMsg
  }

  private[execute] def notifyError(errorMsg: String, t: Throwable): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    this.errorMsg = errorMsg
    this.error = t
  }

  private[execute] def getJobId: Option[String] = {
//    val jobId = request.getProperties.get(JobExecuteRequestInterceptor.PROPERTY_JOB_ID)
    val jobId = request match {
      case entranceExecutorRequest: EntranceExecuteRequest =>
        entranceExecutorRequest.getJob.getId
      case _ =>
        null
    }
    jobId match {
      case j: String => Option(j)
      case _ => None
    }
  }

  override def notify(rs: ExecuteResponse => Unit): Unit = {
    notifyJob = rs
//    this synchronized notify()
  }
}