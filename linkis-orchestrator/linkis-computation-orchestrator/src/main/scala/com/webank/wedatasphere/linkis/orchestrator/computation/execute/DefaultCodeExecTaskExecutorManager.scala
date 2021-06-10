/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.execute

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.{LinkisRetryException, WarnException}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.entrance.LoadBalanceLabel
import com.webank.wedatasphere.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import com.webank.wedatasphere.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import com.webank.wedatasphere.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.{DefaultMarkReq, LoadBanlanceMarkReq, Mark, MarkReq, Policy}
import com.webank.wedatasphere.linkis.orchestrator.ecm.{EngineConnManager, EngineConnManagerBuilder}
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorLabelConflictException, OrchestratorUseSameEngineException}
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
    info(s"Start to askExecutor for execId ${execTask.getId}, wait $wait")
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
          info(s"Finished to askExecutor for execId ${execTask.getId}")
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
    info(s"Start to askExecutor for execId ${execTask.getId}")
    val executor = createExecutor(execTask)
    info(s"Finished to askExecutor for execId ${execTask.getId}")
    Option(executor)
  }

  override def createExecutor(execTask: CodeLogicalUnitExecTask): CodeExecTaskExecutor = {
    info(s"Start to createExecutor for execId ${execTask.getId}")
    val engineConnManager = getEngineConnManager(execTask.getLabels)
    // CreateMarkReq
    val markReq = createMarkReq(execTask)
    // getMark
    var mark: Mark = null
    val executeOnceLabel = LabelUtil.getExecuteOnceLabel(execTask.getLabels)
    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)
    if (null != executeOnceLabel && null != loadBalanceLabel) {
      throw new OrchestratorLabelConflictException(s"ExecuteOnceLabel : ${markReq.getLabels.get(LabelKeyConstant.EXECUTE_ONCE_KEY)} should not come with LoadBalanceLabel : ${markReq.getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY)}")
    }
    if(executeOnceLabel != null){
      // todo check ExecuteOnceLabel should not come with LoadBalanceLabel And BindEngineLabel
      mark = engineConnManager.createMark(markReq)
    } else {
      mark = engineConnManager.applyMark(markReq)
      val bindEngineLabel = LabelUtil.getBindEngineLabel(execTask.getLabels)
      if (null != bindEngineLabel) {
        if (null == mark ) {
          if (bindEngineLabel.getIsJobGroupHead) {
            mark = engineConnManager.createMark(markReq)
          } else {
            // todo logupdate()
            throw new OrchestratorUseSameEngineException(s"ExecTask with id : ${execTask.getId} has bingEngineLabel : ${bindEngineLabel.getStringValue}, but cannot find the pre-built mark.")
          }
        }
      } else {
        if (null == mark) {
          mark = engineConnManager.createMark(markReq)
        }
      }
    }
    // getEngineConn Executor
    info(s"createExecutor for execId ${execTask.getId} mark id is ${mark.getMarkId()}, user ${mark.getMarkReq.getUser}")
    //TODO LOG job.getLogListener.foreach(_.onLogUpdate(job, "Background is starting a new engine for you, it may take several seconds, please wait"))
    val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark)
    if (null == engineConnExecutor) {
      return null
    }
    val codeExecTaskExecutor = new CodeExecTaskExecutor(engineConnExecutor, execTask, mark)
    execTaskToExecutor.put(execTask.getId, codeExecTaskExecutor)
    info(s"Finished to createExecutor for execId ${execTask.getId}")
    codeExecTaskExecutor
  }

  protected def createMarkReq(execTask: CodeLogicalUnitExecTask): MarkReq = {

    val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)
    val markReq: MarkReq =  if (null != loadBalanceLabel) {
      new LoadBanlanceMarkReq
    } else {
      new DefaultMarkReq
    }
    markReq.registerLabelKey(LabelKeyConstant.BIND_ENGINE_KEY)
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

  override def delete(execTask: CodeLogicalUnitExecTask, executor: CodeExecTaskExecutor): Unit = {
    val jobGroupLabel = LabelUtil.getBindEngineLabel(execTask.getLabels)
    /*val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(execTask.getLabels)
    if (null != loadBalanceLabel) {
      info(s"${execTask.getIDInfo()} task has loadBalanceLabel, Not need to delete executor ${executor.getEngineConnExecutor.getServiceInstance}")
      return
    }*/
    var isEndJob = false
    var jobGroupId = ""
    if (null != jobGroupLabel) {
      isEndJob = jobGroupLabel.getIsJobGroupEnd
      jobGroupId = jobGroupLabel.getJobGroupId
      if (isEndJob) {
        info(s"To delete codeExecTaskExecutor  $executor with id : ${executor.getEngineConnExecutor.getServiceInstance} from execTaskToExecutor for lastjob of jobGroupId : ${jobGroupId}")
        clearExecutorById(executor.getExecTaskId, execTask.getLabels)
      } else {
        info(s"Subjob is not end of JobGroup with id : ${jobGroupId}, we will not delete codeExecTaskExecutor with id : ${executor.getExecTaskId} ")
      }
    } else {
      info(s"To delete codeExecTaskExecutor $executor with id : ${executor.getEngineConnExecutor.getServiceInstance}  from execTaskToExecutor.")
      clearExecutorById(executor.getExecTaskId, execTask.getLabels)
    }
  }

  private def clearExecutorById(id: String, labels: util.List[Label[_]]) = {
    val maybeExecutor = execTaskToExecutor.remove(id)
    maybeExecutor.foreach { codeExecTaskExecutor =>
      info(s"Task $id To release engine ConnExecutor ${codeExecTaskExecutor.getEngineConnExecutor.getServiceInstance}")
      val loadBalanceLabel = LabelUtil.getLoadBalanceLabel(labels)
      if (null == loadBalanceLabel) {
        getEngineConnManager(labels).releaseEngineConnExecutor(codeExecTaskExecutor.getEngineConnExecutor, codeExecTaskExecutor.getMark)
      } else {
        info(s"${id} task has loadBalanceLabel, Not need to delete executor ${codeExecTaskExecutor.getEngineConnExecutor.getServiceInstance}")
      }
      removeExecutorFromInstanceToExecutors(codeExecTaskExecutor)
    }
  }

  //private def getJobGroupIdKey(jobGroupId: String): String = LabelKeyConstant.JOB_GROUP_ID + "_" + jobGroupId

  private def removeExecutorFromInstanceToExecutors(executor: CodeExecTaskExecutor): Unit = {
    info(s"To delete codeExecTaskExecutor  $executor from instanceToExecutors")
    val maybeExecutors = instanceToExecutors.get(executor.getEngineConnExecutor.getServiceInstance)
    if (maybeExecutors.isDefined) {
      val executors = maybeExecutors.get.filter(_.getEngineConnTaskId != executor.getEngineConnTaskId)
      if (null != executors && executors.nonEmpty) {
        instanceToExecutors.put(executor.getEngineConnExecutor.getServiceInstance, executors)
      }
    }
  }

  override def addEngineConnTaskID(executor: CodeExecTaskExecutor): Unit = {
    if (execTaskToExecutor.contains(executor.getExecTaskId)) {
      val codeExecutor = new CodeExecTaskExecutor(executor.getEngineConnExecutor, executor.getExecTask, executor.getMark)
      codeExecutor.setEngineConnTaskId(executor.getEngineConnTaskId)
      execTaskToExecutor.put(executor.getExecTaskId, codeExecutor)
      info(s"To add codeExecTaskExecutor  $executor to instanceToExecutors")
      val executors = instanceToExecutors.getOrElse(executor.getEngineConnExecutor.getServiceInstance, Array.empty[CodeExecTaskExecutor])
      instanceToExecutors.put(executor.getEngineConnExecutor.getServiceInstance, executors.+:(codeExecutor))
    }
  }

  private def getEngineConnManager(labels: util.List[Label[_]]): EngineConnManager = {
    if(null == labels || labels.isEmpty) return defaultEngineConnManager
    if(labels.asScala.exists(_.isInstanceOf[LoadBalanceLabel])){
      return labelEngineConnManager
    }
    defaultEngineConnManager
  }

  override def getAllInstanceToExecutorCache(): mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]] = instanceToExecutors

  override def getAllExecTaskToExecutorCache(): mutable.HashMap[String, CodeExecTaskExecutor] = execTaskToExecutor

  override def unLockEngineConn(execTask: CodeLogicalUnitExecTask, execTaskExecutor: CodeExecTaskExecutor): Unit = {
    info(s"${execTask.getIDInfo()} task be killed or failed , Now to delete executor ${execTaskExecutor.getEngineConnExecutor.getServiceInstance}")
    clearExecutorById(execTaskExecutor.getExecTaskId, execTask.getLabels)
  }

}
