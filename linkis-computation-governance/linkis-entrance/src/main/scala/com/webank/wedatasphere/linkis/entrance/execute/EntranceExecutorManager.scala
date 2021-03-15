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

import java.util
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.orchestrator.ecm.EngineConnManager
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.{DefaultMarkReq, MarkReq, Policy}
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.impl.ComputationConcurrentEngineConnExecutor
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorManager}
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration


abstract class EntranceExecutorManager(groupFactory: GroupFactory, val engineConnManager: EngineConnManager) extends ExecutorManager with Logging {

  private val idGenerator = new AtomicLong(0)

  private val idToEngines = new util.HashMap[Long, EntranceExecutor]

  private val instanceToEngines = new util.HashMap[String, EntranceExecutor]

  def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor]

  override def delete(executor: Executor): Unit = {
    if (null != executor) {
      executor.close()
      val entranceExecutor = idToEngines.remove(executor.getId)
      instanceToEngines.remove(entranceExecutor.getInstance.getInstance)
      entranceExecutor.getEngineConnExecutor() match {
        case current: ComputationConcurrentEngineConnExecutor =>
        case _ => engineConnManager.releaseEngineConnExecutor(entranceExecutor.getEngineConnExecutor(), entranceExecutor.mark)
      }
    }
  }

  protected def createMarkReq(requestPersistTask: RequestPersistTask): MarkReq = {
    val markReq = new DefaultMarkReq
    markReq.setPolicyObj(Policy.Task)
    markReq.setCreateService(requestPersistTask.getCreateService)
    markReq.setDescription(requestPersistTask.getDescription)
    markReq.setEngineConnCount(3)
    val properties = if (requestPersistTask.getParams == null) new util.HashMap[String, String]
    else {
      val startupMap = TaskUtils.getStartupMap(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])
      val properties = new JMap[String, String]
      startupMap.foreach { case (k, v) => if (v != null && StringUtils.isNotEmpty(v.toString)) properties.put(k, v.toString) }
      properties
    }
    // todo get default config from db
    markReq.setProperties(properties)
    markReq.setUser(requestPersistTask.getUmUser)
    markReq.setLabels(LabelUtils.labelsToMap(requestPersistTask.getLabels))
    markReq
  }


  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match {
    case job: Job =>
      val executor = createExecutor(job)
      if (executor != null) {
        job match {
          case entranceExecutionJob: EntranceExecutionJob => val task = entranceExecutionJob.getTask
            task.asInstanceOf[RequestPersistTask].setEngineStartTime(new Date())
          case _ =>
        }
        Some(executor)
      } else None

  }

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = schedulerEvent match {
    case job: Job =>
      val startTime = System.currentTimeMillis()
      var warnException: WarnException = null
      var executor: Option[Executor] = None
      while (System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty)
        Utils.tryCatch(askExecutor(job)) {
          case warn: WarnException =>
            this.warn("request engine failed!", warn)
            warnException = warn
            None
          case t: Throwable => throw t
        } match {
          case Some(e) => executor = Option(e)
          case _ =>
            if (System.currentTimeMillis - startTime < wait.toMillis) {
              val interval = math.min(3000, wait.toMillis - System.currentTimeMillis + startTime)
              //getOrCreateEngineManager().waitForIdle(interval)
            }
        }
      if (warnException != null && executor.isEmpty) throw warnException
      executor
  }

  override def getById(id: Long): Option[Executor] = {
    Option(idToEngines.get(id))
  }

  override def getByGroup(groupName: String): Array[Executor] = {
    //TODO by peaceWong
    null
  }

  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceExecutor = schedulerEvent match {
    case job: EntranceJob =>
      job.getTask match {
        case requestPersistTask: RequestPersistTask =>
          // CreateMarkReq
          val markReq = createMarkReq(requestPersistTask)
          // getMark
          val mark = engineConnManager.applyMark(markReq)
          // getEngineConn Executor
          job.getLogListener.foreach(_.onLogUpdate(job, "Background is starting a new engine for you, it may take several seconds, please wait"))
          val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark)
          //TODO 修改Executor创建为builder模式
          val entranceEntranceExecutor = new DefaultEntranceExecutor(idGenerator.incrementAndGet(), mark)
          idToEngines.put(entranceEntranceExecutor.getId, entranceEntranceExecutor)
          instanceToEngines.put(engineConnExecutor.getServiceInstance.getInstance, entranceEntranceExecutor)
          entranceEntranceExecutor.setEngineConnExecutor(engineConnExecutor)
          entranceEntranceExecutor.setInterceptors(getOrCreateInterceptors())
          job.getLogListener.foreach(_.onLogUpdate(job, s" Congratulations! Your new engine has started successfully，Engine are ${engineConnExecutor.getServiceInstance}"))
          entranceEntranceExecutor
        case _ =>
          throw new EntranceErrorException(20001, "Task is not requestPersistTask, cannot to create Executor")
      }
    case _ =>
      throw new EntranceErrorException(20001, "Task is not EntranceJob, cannot to create Executor")
  }

  override def shutdown(): Unit = {}

  def getEntranceExecutorByInstance(instance: String): Option[EntranceExecutor] = {
    Option(instanceToEngines.get(instance))
  }

}
