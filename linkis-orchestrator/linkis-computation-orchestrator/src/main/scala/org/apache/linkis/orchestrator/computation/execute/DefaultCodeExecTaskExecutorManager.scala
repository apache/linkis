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

package org.apache.linkis.orchestrator.computation.execute

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.entrance.LoadBalanceLabel
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.ecm.{EngineConnManager, EngineConnManagerBuilder}
import org.apache.linkis.orchestrator.ecm.entity._
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorLabelConflictException,
  OrchestratorRetryException
}
import org.apache.linkis.orchestrator.listener.task.TaskLogEvent

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class DefaultCodeExecTaskExecutorManager extends CodeExecTaskExecutorManager with Logging {

  private val instanceToExecutors =
    new util.concurrent.ConcurrentHashMap[EngineConnTaskInfo, CodeExecTaskExecutor]

  private val execTaskToExecutor =
    new util.concurrent.ConcurrentHashMap[String, CodeExecTaskExecutor]()

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

  override def askExecutor(
      execTask: CodeLogicalUnitExecTask,
      wait: Duration
  ): Option[CodeExecTaskExecutor] = {
    logger.info(s"Start to askExecutor for execId ${execTask.getIDInfo()}, wait $wait")
    val startTime = System.currentTimeMillis()
    var retryException: LinkisRetryException = null
    var executor: Option[CodeExecTaskExecutor] = None
    while (System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty)
      Utils.tryCatch(askExecutor(execTask)) {
        case retry: LinkisRetryException =>
          logger.warn("request engine failed!", retry)
          retryException = retry
          None
        case t: Throwable => throw t
      } match {
        case Some(e) =>
          logger.info(s"Finished to askExecutor for execId ${execTask
            .getIDInfo()}, wait ${System.currentTimeMillis() - startTime}")
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
    logger.debug(s"Start to askExecutor for execId ${execTask.getIDInfo()}")
    val executor = createExecutor(execTask)
    logger.info(s"Finished to askExecutor for execId ${execTask.getIDInfo()}")
    Option(executor)
  }

  override def createExecutor(execTask: CodeLogicalUnitExecTask): CodeExecTaskExecutor = {

    val engineConnManager = getEngineConnManager(execTask.getLabels)
    // CreateMarkReq
    val markReq = createMarkReq(execTask)

    val executeOnceLabel = LabelUtil.getExecuteOnceLabel(execTask.getLabels)
    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)

    if (null != executeOnceLabel && null != loadBalanceLabel) {
      throw new OrchestratorLabelConflictException(
        s"ExecuteOnceLabel : ${markReq.getLabels.get(LabelKeyConstant.EXECUTE_ONCE_KEY)} should not come with LoadBalanceLabel : ${markReq.getLabels
          .get(LabelKeyConstant.LOAD_BALANCE_KEY)}"
      )
    }

    // getMark
    val mark: Mark = engineConnManager.applyMark(markReq)
    markReq.setCreateService(markReq.getCreateService + s"mark_id: ${mark.getMarkId()}")
    // getEngineConn Executor
    logger.info(s"create Executor for execId ${execTask.getIDInfo()} mark id is ${mark
      .getMarkId()}, user ${mark.getMarkReq.getUser}")
    execTask.getPhysicalContext.pushLog(
      TaskLogEvent(
        execTask,
        LogUtils.generateInfo(s"Background is starting a new engine for you,execId ${execTask
          .getIDInfo()} mark id is ${mark.getMarkId()}, it may take several seconds, please wait")
      )
    )
    val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark, execTask)
    if (null == engineConnExecutor) {
      return null
    }
    val codeExecTaskExecutor = new CodeExecTaskExecutor(engineConnExecutor, execTask, mark)
    if (null != codeExecTaskExecutor) {
      execTaskToExecutor.put(execTask.getId, codeExecTaskExecutor)
    }
    logger.info(s"Finished to create Executor for execId ${execTask.getIDInfo()} mark id is ${mark
      .getMarkId()}, user ${mark.getMarkReq.getUser}")
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
    // markReq.setDescription
    markReq.setEngineConnCount(ComputationOrchestratorConf.DEFAULT_MARK_MAX_ENGINE.getValue)
    val properties =
      if (execTask.getParams.getStartupParams == null) new util.HashMap[String, String]
      else {
        val startupMap = execTask.getParams.getStartupParams.getConfigurationMap().asScala
        val properties = new util.HashMap[String, String]
        startupMap.foreach { case (k, v) =>
          if (v != null && StringUtils.isNotEmpty(v.toString)) properties.put(k, v.toString)
        }
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

  override def getByEngineConnAndTaskId(
      serviceInstance: ServiceInstance,
      engineConnTaskId: String
  ): Option[CodeExecTaskExecutor] = {

    val maybeExecutors =
      instanceToExecutors.get(EngineConnTaskInfo(serviceInstance, engineConnTaskId))
    if (null != maybeExecutors && maybeExecutors.getEngineConnTaskId == engineConnTaskId) {
      Some(maybeExecutors)
    } else None
  }

  override def getByExecTaskId(execTaskId: String): Option[CodeExecTaskExecutor] = {
    val executor = execTaskToExecutor.get(execTaskId)
    Option(executor)
  }

  override def shutdown(): Unit = {}

  /**
   * The method is used to clean up the executor, here will trigger the unlock of ec, but if it is
   * with the loadBlance tag, the unlock step will be skipped
   *
   * @param executor
   * @param labels
   */
  private def clearExecutorById(
      executor: CodeExecTaskExecutor,
      labels: util.List[Label[_]],
      forceRelease: Boolean = false
  ): Unit = {
    if (null == executor || executor.getEngineConnExecutor == null) return
    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(labels)
    if (null == loadBalanceLabel || forceRelease) {
      logger.info(s"To release engine ConnExecutor ${executor}")
      Utils.tryAndWarn {
        getEngineConnManager(labels).releaseEngineConnExecutor(
          executor.getEngineConnExecutor,
          executor.getMark
        )
      }
    } else {
      logger.info(s"Task has loadBalanceLabel, Not need to delete executor ${executor}")
    }
    removeExecutorFromInstanceToExecutors(executor)
  }

  private def removeExecutorFromInstanceToExecutors(executor: CodeExecTaskExecutor): Unit = {
    logger.debug(s"To delete codeExecTaskExecutor  ${executor} from instanceToExecutors")
    val engineConnTaskInfo = EngineConnTaskInfo(
      executor.getEngineConnExecutor.getServiceInstance,
      executor.getEngineConnTaskId
    )
    instanceToExecutors.remove(engineConnTaskInfo)
    execTaskToExecutor.remove(executor.getExecTaskId)
    logger.info(
      s"To delete exec task ${executor.getExecTask.getIDInfo()} and CodeExecTaskExecutor ${executor.getEngineConnExecutor.getServiceInstance} relation"
    )
  }

  override def addEngineConnTaskInfo(executor: CodeExecTaskExecutor): Unit = {
    if (null == executor || StringUtils.isBlank(executor.getExecTaskId)) {
      throw new OrchestratorRetryException(
        OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
        "Failed to store task information"
      )
    }
    val engineConnTaskInfo = EngineConnTaskInfo(
      executor.getEngineConnExecutor.getServiceInstance,
      executor.getEngineConnTaskId
    )
    instanceToExecutors.put(engineConnTaskInfo, executor)
    logger.info(s"Finished To add codeExecTaskExecutor  $executor to instanceToExecutors")
  }

  private def getEngineConnManager(labels: util.List[Label[_]]): EngineConnManager = {
    if (null == labels || labels.isEmpty) return defaultEngineConnManager
    if (labels.asScala.exists(_.isInstanceOf[LoadBalanceLabel])) {
      return labelEngineConnManager
    }
    defaultEngineConnManager
  }

  override def getAllInstanceToExecutorCache(): util.Map[EngineConnTaskInfo, CodeExecTaskExecutor] =
    instanceToExecutors

  override def getAllExecTaskToExecutorCache(): util.Map[String, CodeExecTaskExecutor] =
    execTaskToExecutor

  override protected def unLockEngineConn(
      execTask: CodeLogicalUnitExecTask,
      execTaskExecutor: CodeExecTaskExecutor
  ): Unit = {
    logger.info(
      s"${execTask.getIDInfo()} task be killed or failed , Now to delete executor ${execTaskExecutor.getEngineConnExecutor.getServiceInstance}"
    )
    clearExecutorById(execTaskExecutor, execTask.getLabels)
  }

  override protected def markECFailed(
      execTask: CodeLogicalUnitExecTask,
      executor: CodeExecTaskExecutor
  ): Unit = {
    logger.info(
      s"${execTask.getIDInfo()} task  failed because executor exit, Now to delete executor ${executor.getEngineConnExecutor.getServiceInstance}"
    )
    clearExecutorById(executor, execTask.getLabels, true)
  }

  override protected def delete(
      execTask: CodeLogicalUnitExecTask,
      executor: CodeExecTaskExecutor
  ): Unit = {
    val jobGroupLabel = LabelUtil.getBindEngineLabel(execTask.getLabels)
    var isEndJob = false
    var jobGroupId = ""
    if (null != jobGroupLabel) {
      isEndJob = jobGroupLabel.getIsJobGroupEnd
      jobGroupId = jobGroupLabel.getJobGroupId
      if (isEndJob) {
        logger.debug(
          s"To delete codeExecTaskExecutor  $executor from execTaskToExecutor for lastjob of jobGroupId : ${jobGroupId}"
        )
        clearExecutorById(executor, execTask.getLabels)
      } else {
        removeExecutorFromInstanceToExecutors(executor)
        logger.info(
          s"Subjob is not end of JobGroup with id : ${jobGroupId}, we will not delete codeExecTaskExecutor with id : ${executor} "
        )
      }
    } else {
      logger.debug(s"To delete codeExecTaskExecutor  ${executor}  from execTaskToExecutor.")
      clearExecutorById(executor, execTask.getLabels)
    }
  }

  override def markTaskCompleted(
      execTask: CodeLogicalUnitExecTask,
      executor: CodeExecTaskExecutor,
      isSucceed: Boolean
  ): Unit = {
    if (isSucceed) {
      logger.debug(s"ExecTask(${execTask.getIDInfo()}) execute  success executor be delete.")
      Utils.tryAndWarn(delete(execTask, executor))
    } else {
      if (StringUtils.isBlank(executor.getEngineConnTaskId)) {
        logger.error(
          s"${execTask.getIDInfo()} Failed to submit running, now to remove  codeEngineConnExecutor, forceRelease"
        )
        Utils.tryAndWarn(markECFailed(execTask, executor))
      } else {
        logger.debug(s"ExecTask(${execTask.getIDInfo()}) execute  failed executor be unLock.")
        Utils.tryAndWarn(unLockEngineConn(execTask, executor))
      }
    }
    if (null != executor && executor.getEngineConnExecutor != null) {
      executor.getEngineConnExecutor.removeTask(executor.getEngineConnTaskId)
    }
  }

}
