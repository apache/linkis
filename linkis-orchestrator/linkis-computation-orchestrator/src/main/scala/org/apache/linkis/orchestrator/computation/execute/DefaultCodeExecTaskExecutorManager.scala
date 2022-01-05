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
 
package org.apache.linkis.orchestrator.computation.execute

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.{LinkisRetryException, WarnException}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.entrance.LoadBalanceLabel
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.ecm.entity.{DefaultMarkReq, LoadBanlanceMarkReq, Mark, MarkReq, Policy}
import org.apache.linkis.orchestrator.ecm.{EngineConnManager, EngineConnManagerBuilder}
import org.apache.linkis.orchestrator.exception.{OrchestratorLabelConflictException, OrchestratorUseSameEngineException}
import org.apache.linkis.orchestrator.listener.task.TaskLogEvent
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration.Duration

/**
  *
  */
class DefaultCodeExecTaskExecutorManager extends CodeExecTaskExecutorManager with Logging {

  private val instanceToExecutors = new mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]]

  private val execTaskToExecutor = new mutable.HashMap[String, CodeExecTaskExecutor]()

  private val defaultEngineConnManager: EngineConnManager = {
    val builder = EngineConnManagerBuilder.builder
    builder.setPolicy(Policy.Process)
    builder.build()
  }
  private val labelEngineConnManager: EngineConnManager = {
    val builder = EngineConnManagerBuilder.builder
    builder.setPolicy(Policy.Label)
    builder.build()
  }
  private val waitLock = new Array[Byte](0)

  override def askExecutor(execTask: CodeLogicalUnitExecTask, wait: Duration): Option[CodeExecTaskExecutor] = {
    info(s"Start to askExecutor for execId ${execTask.getIDInfo()}, wait $wait")
    val startTime = System.currentTimeMillis()
    var retryException: LinkisRetryException = null
    var executor: Option[CodeExecTaskExecutor] = None
    while (System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty)
      Utils.tryCatch(askExecutor(execTask)) {
        case retry: LinkisRetryException =>
          this.warn("request engine failed!", retry)
          retryException = retry
          None
        case t: Throwable => throw t
      } match {
        case Some(e) =>
          info(s"Finished to askExecutor for execId ${execTask.getIDInfo()}, wait ${System.currentTimeMillis() - startTime}")
          executor = Option(e)
        case _ =>
          if (System.currentTimeMillis - startTime < wait.toMillis) {
            val interval = math.min(3000, wait.toMillis - System.currentTimeMillis + startTime)
            waitForIdle(interval)
          }
      }
    if (retryException != null && executor.isEmpty) throw retryException
    executor
  }

  override def askExecutor(execTask: CodeLogicalUnitExecTask): Option[CodeExecTaskExecutor] = {
    debug(s"Start to askExecutor for execId ${execTask.getIDInfo()}")
    val executor = createExecutor(execTask)
    info(s"Finished to askExecutor for execId ${execTask.getIDInfo()}")
    Option(executor)
  }

  override def createExecutor(execTask: CodeLogicalUnitExecTask): CodeExecTaskExecutor = {

    val engineConnManager = getEngineConnManager(execTask.getLabels)
    // CreateMarkReq
    val markReq = createMarkReq(execTask)

    val executeOnceLabel = LabelUtil.getExecuteOnceLabel(execTask.getLabels)
    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)

    if (null != executeOnceLabel && null != loadBalanceLabel) {
      throw new OrchestratorLabelConflictException(s"ExecuteOnceLabel : ${markReq.getLabels.get(LabelKeyConstant.EXECUTE_ONCE_KEY)} should not come with LoadBalanceLabel : ${markReq.getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY)}")
    }

    // getMark
    val mark: Mark = engineConnManager.applyMark(markReq)
    markReq.setCreateService(markReq.getCreateService + s"mark_id: ${mark.getMarkId()}")
    // getEngineConn Executor
    info(s"create Executor for execId ${execTask.getIDInfo()} mark id is ${mark.getMarkId()}, user ${mark.getMarkReq.getUser}")
    execTask.getPhysicalContext.pushLog(TaskLogEvent(execTask, LogUtils.generateInfo(s"Background is starting a new engine for you,execId ${execTask.getIDInfo()} mark id is ${mark.getMarkId()}, it may take several seconds, please wait")))
    val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark)
    if (null == engineConnExecutor) {
      return null
    }
    val codeExecTaskExecutor = new CodeExecTaskExecutor(engineConnExecutor, execTask, mark)
    execTaskToExecutor synchronized {
      execTaskToExecutor.put(execTask.getId, codeExecTaskExecutor)
    }
    info(s"Finished to create Executor for execId ${execTask.getIDInfo()} mark id is ${mark.getMarkId()}, user ${mark.getMarkReq.getUser}")
    codeExecTaskExecutor
  }

  protected def createMarkReq(execTask: CodeLogicalUnitExecTask): MarkReq = {

    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)
    val markReq: MarkReq = if (null != loadBalanceLabel) {
      new LoadBanlanceMarkReq
    } else {
      val defaultMarkReq = new DefaultMarkReq
      defaultMarkReq.registerLabelKey(LabelKeyConstant.BIND_ENGINE_KEY)
      defaultMarkReq
    }
    markReq.setPolicyObj(Policy.Task)
    markReq.setCreateService(ComputationOrchestratorConf.DEFAULT_CREATE_SERVICE.getValue)
    //markReq.setDescription
    markReq.setEngineConnCount(ComputationOrchestratorConf.DEFAULT_MARK_MAX_ENGINE.getValue)
    val properties = if (execTask.getParams.getStartupParams == null) new util.HashMap[String, String]
    else {
      val startupMap = execTask.getParams.getStartupParams.getConfigurationMap().asScala
      val properties = new util.HashMap[String, String]
      startupMap.foreach { case (k, v) => if (v != null && StringUtils.isNotEmpty(v.toString)) properties.put(k, v.toString) }
      properties
    }

    markReq.setProperties(properties)
    markReq.setUser(execTask.getExecuteUser)
    if (null != execTask.getLabels) {
      markReq.setLabels(LabelUtils.labelsToMap(execTask.getLabels))
    }
    markReq
  }

  private def waitForIdle(waitTime: Long): Unit = waitLock synchronized {
    waitLock.wait(waitTime)
  }

  override def getByEngineConnAndTaskId(serviceInstance: ServiceInstance, engineConnTaskId: String): Option[CodeExecTaskExecutor] = {
    val maybeExecutors = instanceToExecutors.get(serviceInstance)
    if (maybeExecutors.isDefined) {
      val executors = maybeExecutors.get.filter(_.getEngineConnTaskId == engineConnTaskId)
      if (null != executors && executors.nonEmpty) {
        return Some(executors(0))
      }
    }
    None
  }

  override def getByExecTaskId(execTaskId: String): Option[CodeExecTaskExecutor] = {
    execTaskToExecutor.get(execTaskId)
  }

  override def shutdown(): Unit = {

  }

  /**
   * The job execution process is normal. After the job is completed, you can call this method.
   * This method will determine the bind engine label. If it is a non-end type job, no operation will be performed.
   *
   * @param execTask
   * @param executor
   */
  override def delete(execTask: CodeLogicalUnitExecTask, executor: CodeExecTaskExecutor): Unit = {
    val jobGroupLabel = LabelUtil.getBindEngineLabel(execTask.getLabels)
    var isEndJob = false
    var jobGroupId = ""
    if (null != jobGroupLabel) {
      isEndJob = jobGroupLabel.getIsJobGroupEnd
      jobGroupId = jobGroupLabel.getJobGroupId
      if (isEndJob) {
        debug(s"To delete codeExecTaskExecutor  $executor from execTaskToExecutor for lastjob of jobGroupId : ${jobGroupId}")
        clearExecutorById(executor, execTask.getLabels)
      } else {
        removeExecutorFromInstanceToExecutors(executor)
        info(s"Subjob is not end of JobGroup with id : ${jobGroupId}, we will not delete codeExecTaskExecutor with id : ${executor} ")
      }
    } else {
      debug(s"To delete codeExecTaskExecutor  ${executor}  from execTaskToExecutor.")
      clearExecutorById(executor, execTask.getLabels)
    }
  }

  /**
   * The method is used to clean up the executor, here will trigger the unlock of ec,
   * but if it is with the loadBlance tag, the unlock step will be skipped
   *
   * @param executor
   * @param labels
   */
  private def clearExecutorById(executor: CodeExecTaskExecutor, labels: util.List[Label[_]], forceRelease: Boolean = false): Unit = {
    if (null == executor || executor.getEngineConnExecutor == null) return
    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(labels)
    if (null == loadBalanceLabel || forceRelease) {
      info(s"To release engine ConnExecutor ${executor}")
      Utils.tryAndWarn {
        getEngineConnManager(labels).releaseEngineConnExecutor(executor.getEngineConnExecutor, executor.getMark)
      }
    } else {
      info(s"Task has loadBalanceLabel, Not need to delete executor ${executor}")
    }
    removeExecutorFromInstanceToExecutors(executor)
  }


  private def removeExecutorFromInstanceToExecutors(executor: CodeExecTaskExecutor): Unit = {
    debug(s"To delete codeExecTaskExecutor  ${executor} from instanceToExecutors")
    val maybeExecutors = instanceToExecutors.get(executor.getEngineConnExecutor.getServiceInstance)
    if (maybeExecutors.isDefined) {
      val executors = maybeExecutors.get.filter(_.getEngineConnTaskId != executor.getEngineConnTaskId)
      instanceToExecutors synchronized {
        if (null != executors && executors.nonEmpty) {
          instanceToExecutors.put(executor.getEngineConnExecutor.getServiceInstance, executors)
        } else {
          instanceToExecutors.remove(executor.getEngineConnExecutor.getServiceInstance)
        }
      }
    }
    info(s"To delete exec task ${executor.getExecTask.getIDInfo()} and CodeExecTaskExecutor ${executor.getEngineConnExecutor.getServiceInstance} relation")
    execTaskToExecutor synchronized {
      execTaskToExecutor.remove(executor.getExecTaskId)
    }
  }

  override def addEngineConnTaskID(executor: CodeExecTaskExecutor): Unit = {
    /* val codeExecutor = new CodeExecTaskExecutor(executor.getEngineConnExecutor, executor.getExecTask, executor.getMark)
     codeExecutor.setEngineConnTaskId(executor.getEngineConnTaskId)*/
    execTaskToExecutor synchronized {
      execTaskToExecutor.put(executor.getExecTaskId, executor)
    }
    info(s"To add codeExecTaskExecutor  $executor to instanceToExecutors")
    val executors = instanceToExecutors.getOrElse(executor.getEngineConnExecutor.getServiceInstance, Array.empty[CodeExecTaskExecutor])
    instanceToExecutors synchronized {
      instanceToExecutors.put(executor.getEngineConnExecutor.getServiceInstance, executors.+:(executor))
    }
  }

  private def getEngineConnManager(labels: util.List[Label[_]]): EngineConnManager = {
    if (null == labels || labels.isEmpty) return defaultEngineConnManager
    if (labels.asScala.exists(_.isInstanceOf[LoadBalanceLabel])) {
      return labelEngineConnManager
    }
    defaultEngineConnManager
  }

  override def getAllInstanceToExecutorCache(): mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]] = instanceToExecutors

  override def getAllExecTaskToExecutorCache(): mutable.HashMap[String, CodeExecTaskExecutor] = execTaskToExecutor

  /**
   * If the job is executed abnormally, such as execution failure, or being killed,
   * it will go to the process for cleaning up, and the engineConn lock will be released.
   *
   * @param execTask
   * @param execTaskExecutor
   */
  override def unLockEngineConn(execTask: CodeLogicalUnitExecTask, execTaskExecutor: CodeExecTaskExecutor): Unit = {
    info(s"${execTask.getIDInfo()} task be killed or failed , Now to delete executor ${execTaskExecutor.getEngineConnExecutor.getServiceInstance}")
    clearExecutorById(execTaskExecutor, execTask.getLabels)
  }

  /**
   * Task failed because ec exited unexpectedly, so need to clean up ec immediately
   *
   * @param execTask
   * @param executor
   */
  override def markECFailed(execTask: CodeLogicalUnitExecTask, executor: CodeExecTaskExecutor): Unit = {
    info(s"${execTask.getIDInfo()} task  failed because executor exit, Now to delete executor ${executor.getEngineConnExecutor.getServiceInstance}")
    clearExecutorById(executor, execTask.getLabels, true)
  }
}
