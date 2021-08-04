/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.orchestrator.Orchestration
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorManager}
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}

import java.util
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.Duration


abstract class EntranceExecutorManager(groupFactory: GroupFactory) extends ExecutorManager with Logging {

  private val idGenerator = new AtomicLong(0)

//  private val idToEngines = new util.HashMap[Long, EntranceExecutor]

//  private val instanceToEngines = new util.HashMap[String, EntranceExecutor]

  private val orchestrationToJobs = new ConcurrentHashMap[Orchestration, EntranceJob]()

  private val orchestrationToJobReturns = new ConcurrentHashMap[Orchestration, EngineExecuteAsynReturn]

  private val jobIdToEntranceJobs = new ConcurrentHashMap[String, EntranceJob]()

  def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor]

  override def delete(executor: Executor): Unit = {
    if (null != executor) {
      executor.close()
      /*val entranceExecutor = idToEngines.remove(executor.getId)
//      instanceToEngines.remove(entranceExecutor.getInstance.getInstance)
      entranceExecutor.getOrchestratorSession() match {
        case current: ComputationConcurrentEngineConnExecutor =>
        case _ => engineConnManager.releaseEngineConnExecutor(entranceExecutor.getOrchestratorSession(), entranceExecutor.mark)
      }*/
    }
  }

  protected def createMarkReq(jobReq: JobRequest): MarkReq = {
    val markReq = new MarkReq
    markReq.setCreateService(EntranceConfiguration.DEFAULT_CREATE_SERVICE.getValue)
    // todo get default config from db
    markReq.setProperties(jobReq.getParams)
    markReq.setUser(jobReq.getSubmitUser)
    markReq.setLabels(LabelUtils.labelsToMap(jobReq.getLabels))
    markReq
  }


  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match {
    case job: Job =>
      val executor = createExecutor(job)
      if (executor != null) {
        job match {
          case entranceExecutionJob: EntranceExecutionJob =>
            val jobReq = entranceExecutionJob.getJobRequest
            jobReq.setUpdatedTime(new Date(System.currentTimeMillis()))
          case _ =>
        }
        Some(executor)
      } else None

  }

  // todo 提交任务逻辑调整：将job切分成多条语句，塞到jobGroup队列中。任务提交后，按照队列先后顺序，依次执行任务；
  // 没个子任务运行后，更新整体的Job运行状态
  // 直到所有任务都完毕，或者存在任务异常退出，则结束整体的Job

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = schedulerEvent match {
    case job: Job =>
      val startTime = System.currentTimeMillis()
      var warnException: WarnException = null
      var executor: Option[Executor] = None
      while (System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty)
        Utils.tryCatch(executor = askExecutor(job)) {
          case warn: WarnException =>
            this.warn("request engine failed!", warn)
            warnException = warn
            None
          case t: Throwable => throw t
        } /*match {
          case Some(e) => executor = Option(e)
          case _ =>
            if (System.currentTimeMillis - startTime < wait.toMillis) {
              val interval = math.min(3000, wait.toMillis - System.currentTimeMillis + startTime)
              //getOrCreateEngineManager().waitForIdle(interval)
            }
        }*/
      // todo check
      if (warnException != null && executor.isEmpty) throw warnException
      executor
  }

  override def getById(id: Long): Option[Executor] = {
//    Option(idToEngines.get(id))o
    null
  }

  override def getByGroup(groupName: String): Array[Executor] = {
    //TODO by peaceWong
    null
  }

  // todo  获取Orchestrator session； 切分job； 提交jobGroup；
  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceExecutor = schedulerEvent match {
    case job: EntranceJob =>
      job.getJobRequest match {
        case jobRequest: JobRequest =>
          // CreateMarkReq
          val markReq = createMarkReq(jobRequest)
          // getMark
          val entranceEntranceExecutor = new DefaultEntranceExecutor(idGenerator.incrementAndGet(), markReq, this)
          // getEngineConn Executor
          job.getLogListener.foreach(_.onLogUpdate(job, "Your job is being scheduled by orchestrator."))
          /**
//          val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark)
          idToEngines.put(entranceEntranceExecutor.getId, entranceEntranceExecutor)*/
//          instanceToEngines.put(engineConnExecutor.getServiceInstance.getInstance, entranceEntranceExecutor) // todo
//          entranceEntranceExecutor.setInterceptors(getOrCreateInterceptors()) // todo
          entranceEntranceExecutor
        case _ =>
          throw new EntranceErrorException(20001, "Task is not requestPersistTask, cannot to create Executor")
      }
    case _ =>
      throw new EntranceErrorException(20001, "Task is not EntranceJob, cannot to create Executor")
  }

  override def shutdown(): Unit = {}

  def getEntranceExecutorByInstance(instance: String): Option[EntranceExecutor] = {
//    Option(instanceToEngines.get(instance))
    null
  }

  def getEntranceJobByOrchestration(orchestration: Orchestration): Option[EntranceJob] = {
    Option(orchestrationToJobs.get(orchestration))
  }

  def setOrchestrationAndEntranceJob(orchestration: Orchestration, entranceJob: EntranceJob): Unit = {
    orchestrationToJobs.put(orchestration, entranceJob)
  }

  def getAsyncJobReturnByOrchestration(orchestration: Orchestration): Option[EngineExecuteAsynReturn] = {
    Option(orchestrationToJobReturns.get(orchestration))
  }

  def setOrchestrationAndExecutorAsyncReturn(orchestration: Orchestration, engineExecuteAsynReturn: EngineExecuteAsynReturn): Unit = {
    orchestrationToJobReturns.put(orchestration, engineExecuteAsynReturn)
  }

  def clearOrchestrationCache(orchestration: Orchestration): Unit = {
    if (null != orchestration) {
      orchestrationToJobs.remove(orchestration)
      orchestrationToJobReturns.remove(orchestration)
    }
  }

  def getEntranceJobByExecId(execId: String): Option[EntranceJob] = {
    Option(jobIdToEntranceJobs.get(execId))
  }

  def setJobExecIdAndEntranceJob(execId: String, entranceJob: EntranceJob): Unit = {
    jobIdToEntranceJobs.put(execId, entranceJob)
  }

  def clearJobCache(execId: String): Unit = {
    if (null != execId) {
      jobIdToEntranceJobs.remove(execId)
    }
  }
}
