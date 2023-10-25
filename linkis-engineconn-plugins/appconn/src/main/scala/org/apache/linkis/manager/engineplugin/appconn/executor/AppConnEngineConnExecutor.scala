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

package org.apache.linkis.manager.engineplugin.appconn.executor

import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.computation.executor.async.AsyncConcurrentComputationExecutor
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.governance.common.utils.GovernanceConstant
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.appconn.conf.AppConnEngineConnConfiguration
import org.apache.linkis.manager.engineplugin.appconn.exception.AppConnExecuteFailedException
import org.apache.linkis.manager.engineplugin.appconn.executor.AppConnEngineConnExecutor._
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  AsynReturnExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

import java.io.File
import java.util
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import scala.collection.JavaConverters._

import com.webank.wedatasphere.dss.appconn.core.AppConn
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn
import com.webank.wedatasphere.dss.appconn.loader.utils.AppConnUtils
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager
import com.webank.wedatasphere.dss.common.label.{DSSLabel, EnvDSSLabel, LabelKeyConvertor}
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils
import com.webank.wedatasphere.dss.standard.app.development.listener.common.AbstractRefExecutionAction
import com.webank.wedatasphere.dss.standard.app.development.listener.core.Killable
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.{
  AsyncExecutionResponseRef,
  ExecutionResponseRef
}
import com.webank.wedatasphere.dss.standard.app.sso.Workspace
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef

class AppConnEngineConnExecutor(override val outputPrintLimit: Int, val id: Int)
    extends AsyncConcurrentComputationExecutor {

  private var user: String = _

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val taskHashMap: ConcurrentHashMap[String, AsyncExecutionResponseRef] =
    new ConcurrentHashMap[String, AsyncExecutionResponseRef](8)

  // 定时清除map元素
  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = Utils.tryAndError {
        val iterator =
          taskHashMap.entrySet().asScala.filter(_.getValue.isCompleted).map(_.getKey).clone()
        iterator.foreach(taskHashMap.remove(_))
        info(s"Cleaned ${iterator.size} history jobs, now only ${taskHashMap.size()} remained.")
      }

    },
    0,
    5,
    TimeUnit.MINUTES
  )

  def setUser(user: String): Unit = this.user = user

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    info(
      s"The execution code is: $code, jobId is: ${engineExecutorContext.getJobId}, runtime properties is: ${BDPJettyServerHelper.gson
        .toJson(engineExecutorContext.getProperties)}."
    )

    val source =
      engineExecutorContext.getProperties.get(GovernanceConstant.TASK_SOURCE_MAP_KEY) match {
        case map: util.Map[String, Object] => map
        case _ => return ErrorExecuteResponse("Cannot find source.", null)
      }
    def getValue(map: util.Map[String, AnyRef], key: String): String = map.get(key) match {
      case string: String => string
      case anyRef: AnyRef => BDPJettyServerHelper.gson.toJson(anyRef)
      case _ => null
    }

    val workspace = BDPJettyServerHelper.gson.fromJson(
      getValue(engineExecutorContext.getProperties, WORKSPACE_NAME_STR),
      classOf[Workspace]
    )
    val appConnName = getAppConnName(getValue(engineExecutorContext.getProperties, NODE_TYPE))
    val appConn = AppConnManager.getAppConnManager.getAppConn(appConnName)
    if (appConn == null) {
      error(s"Cannot find AppConn $appConnName. jobId is: ${engineExecutorContext.getJobId}")
      throw AppConnExecuteFailedException(510001, "Cannot Find appConnName: " + appConnName)
    }
    val labels = engineExecutorContext.getProperties.get("labels").toString
    getAppInstanceByLabels(labels, appConn) match {
      case Some(appInstance) =>
        val developmentIntegrationStandard =
          appConn.asInstanceOf[OnlyDevelopmentAppConn].getOrCreateDevelopmentStandard
        val refExecutionService = developmentIntegrationStandard.getRefExecutionService(appInstance)
        val refJobContent =
          if (StringUtils.isNotBlank(code))
            BDPJettyServerHelper.gson.fromJson(code, classOf[util.HashMap[String, AnyRef]])
          else engineExecutorContext.getProperties
        var submitUser = getValue(engineExecutorContext.getProperties, SUBMIT_USER_KEY)
        if (submitUser == null) submitUser = user
        val variables = engineExecutorContext.getProperties.get(VARIABLES_KEY) match {
          case map: util.Map[String, Object] => map
          case _ => new util.HashMap[String, Object]()
        }
        info(s"try to execute appconn job for jobId: ${engineExecutorContext.getJobId}")
        val responseRef = Utils.tryCatch {
          AppConnExecutionUtils.tryToOperation(
            refExecutionService,
            getValue(engineExecutorContext.getProperties, CONTEXT_ID_KEY),
            getValue(source, PROJECT_NAME_STR),
            new ExecutionRequestRefContextImpl(engineExecutorContext, user, submitUser),
            getLabels(labels),
            getValue(source, NODE_NAME_STR),
            getValue(engineExecutorContext.getProperties, NODE_TYPE),
            user,
            workspace,
            refJobContent,
            variables
          )
        }(t => ExecutionResponseRef.newBuilder.setException(t).error())
        responseRef match {
          case asyncResponseRef: AsyncExecutionResponseRef =>
            engineExecutorContext.getJobId match {
              case Some(id) =>
                info(s"add async responseRef to task map, the taskId is $id.")
                taskHashMap.put(id, asyncResponseRef)
              case _ =>
            }
            new AsynReturnExecuteResponse {
              private var er: ExecuteResponse => Unit = _

              def tryToNotifyAll(responseRef: ResponseRef): Unit = {
                val executeResponse = createExecuteResponse(responseRef, appConnName)
                if (er == null) this synchronized {
                  while (er == null) this.wait(1000)
                }
                er(executeResponse)
              }

              override def notify(rs: ExecuteResponse => Unit): Unit = {
                er = rs
                this synchronized notifyAll()
              }

              asyncResponseRef.notifyMe(new java.util.function.Consumer[ResponseRef] {
                override def accept(t: ResponseRef): Unit = tryToNotifyAll(t)
              })
            }
          case responseRef: ResponseRef =>
            createExecuteResponse(responseRef, appConnName)
        }
      case None =>
        throw AppConnExecuteFailedException(510000, "Cannot Find AppInstance by labels." + labels)
    }
  }

  private def createExecuteResponse(
      responseRef: ResponseRef,
      appConnName: String
  ): ExecuteResponse =
    if (responseRef.isSucceed) SuccessExecuteResponse()
    else {
      val exception = responseRef match {
        case response: ExecutionResponseRef => response.getException
        case _ => null
      }
      error(s"$appConnName execute failed, failed reason is ${responseRef.getErrorMsg}.", exception)
      ErrorExecuteResponse(responseRef.getErrorMsg, exception)
    }

  private def getAppConnName(nodeType: String) = {
    StringUtils.split(nodeType, ".")(0)
  }

  private def getLabels(labels: String): util.List[DSSLabel] = {
    val envLabelValue =
      if (
          labels.contains(LabelKeyConvertor.ROUTE_LABEL_KEY) || labels.contains(
            EnvDSSLabel.DSS_ENV_LABEL_KEY
          )
      ) {
        val labelMap =
          DSSCommonUtils.COMMON_GSON.fromJson(labels, classOf[util.Map[String, String]])
        labelMap.getOrDefault(
          LabelKeyConvertor.ROUTE_LABEL_KEY,
          labelMap.getOrDefault(EnvDSSLabel.DSS_ENV_LABEL_KEY, labels)
        )
      } else labels
    util.Arrays.asList(new EnvDSSLabel(envLabelValue))
  }

  private def getAppInstanceByLabels(labels: String, appConn: AppConn): Option[AppInstance] = {
    val appInstanceList = appConn.getAppDesc.getAppInstancesByLabels(getLabels(labels))
    if (appInstanceList != null && appInstanceList.size() > 0) {
      return Some(appInstanceList.get(0))
    }
    None
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

  override def progress(taskID: String): Float = 0

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = Array.empty

  override def supportCallBackLogs(): Boolean = false

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (null != labels && !labels.isEmpty) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = null

  override def getCurrentNodeResource(): NodeResource = {
    val properties = EngineConnServer.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(
          EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key,
          settingClientMemory + "g"
        )
      }
    }
    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def getId(): String = "AppConnEngineExecutor_" + id

  override def getConcurrentLimit: Int = AppConnEngineConnConfiguration.CONCURRENT_LIMIT.getValue

  override def killAll(): Unit = {}

  override def killTask(taskID: String): Unit = {
    warn(s"AppConn want to kill job: $taskID.")
    if (taskHashMap.containsKey(taskID)) {
      val response = taskHashMap.get(taskID)
      response.getAction match {
        case action: AbstractRefExecutionAction =>
          action.setKilledFlag(true)
        case _ =>
      }
      taskHashMap.remove(taskID)
      response.getRefExecutionOperation match {
        case killable: Killable =>
          killable.kill(response.getAction)
        case _ =>
      }
      info(s"AppConn Kill job: $taskID succeed.")
    } else {
      warn(s"AppConn Kill job: $taskID failed for task is not exist.")
    }
    super.killTask(taskID)
  }

  override def tryShutdown(): Boolean = {
    val toClearPath = AppConnUtils.getAppConnHomePath
    val file = new File(toClearPath)
    if (!file.exists()) {
      warn(s"the appconn path is not exists, path is: ${toClearPath}")
    }
    info(s"try to clear dss-appconns for this engine, path is: ${toClearPath}")
    Utils.tryCatch(FileUtils.deleteDirectory(file))(t => {
      error(s"you have no permission to delete this path: ${toClearPath}, error: $t")
    })
    super.tryShutdown()
  }

}

object AppConnEngineConnExecutor {

  private val WORKSPACE_NAME_STR = "workspace"

  private val PROJECT_NAME_STR = "projectName"

  private val FLOW_NAME_STR = "flowName"

  private val NODE_NAME_STR = "nodeName"

  private val NODE_TYPE = "nodeType"

  private val CONTEXT_ID_KEY = "contextID"

  private val SUBMIT_USER_KEY = "wds.dss.workflow.submit.user"

  private val VARIABLES_KEY = "variables"

}
