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
 */

package com.webank.wedatasphere.linkis.engineconn.core.executor

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.core.util.EngineConnUtils
import com.webank.wedatasphere.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, LabelExecutor}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{MultiExecutorEngineConnFactory, SingleExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.{EngineConnPluginErrorCode, EngineConnPluginErrorException}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineRunTypeLabel

import scala.collection.JavaConverters._

trait ExecutorManager {

  def getDefaultExecutor: Executor

  def getExecutorByLabels(labels: Array[Label[_]]): Executor

  def createExecutor(engineCreationContext: EngineCreationContext, labels: Array[Label[_]]): Executor

  def getAllExecutorsMap(): util.Map[String, Executor]

  def generateId(): Int

}

class ExecutorManagerImpl extends ExecutorManager with Logging {

  private val executors: util.Map[String, Executor] = new ConcurrentHashMap[String, Executor](2)
  private val GSON = EngineConnUtils.GSON
  private var defaultExecutor: Executor = _

  private val idCreator = new AtomicInteger()

  override def getDefaultExecutor: Executor = {
    if (null == defaultExecutor || defaultExecutor.isClosed()) {
      val engineConn = EngineConnManager.getEngineConnManager.getEngineConn()
      if (null == engineConn) {
        error("Create default executor failed., engineConn not ready")
        return null
      }
      Utils.tryCatch {
        createExecutor(engineConn.getEngineCreationContext, null)
      } {
        case t: Throwable =>
          error("Create default executor failed. Now will try again once.", t)
          createExecutor(engineConn.getEngineCreationContext, null)
      }
    }
    defaultExecutor
  }

  override def createExecutor(engineCreationContext: EngineCreationContext, labels: Array[Label[_]]): Executor = {
    val engineConn = EngineConnManager.getEngineConnManager.getEngineConn()
    val enginePlugin: EngineConnPlugin = EngineConnObject.getEngineConnPlugin
    val isDefault: Boolean = if (null == labels || labels.isEmpty || null == defaultExecutor || defaultExecutor.isClosed()) true else false
    var runType: String = null
    if (!isDefault) {
      runType = getRunTypeFromLables(labels)
      if (null == runType) {
        val msg = "Invalid RunType label. labels : " + GSON.toJson(labels)
        error(msg)
        throw new EngineConnPluginErrorException(EngineConnPluginErrorCode.INVALID_LABELS, msg)
      }
    }
    val executor: Executor = {
      enginePlugin.getEngineConnFactory match {
        case engineConnFactory: SingleExecutorEngineConnFactory =>
          engineConnFactory.createExecutor(engineCreationContext, engineConn)
        case engineConnFactory: MultiExecutorEngineConnFactory =>
          val executorFactories = engineConnFactory.getExecutorFactories
          val chooseExecutorFactory = if (isDefault) {
            info("use default executor")
            Some(engineConnFactory.getDefaultExecutorFactory)
          } else {
            executorFactories.find(e => e.canCreate(labels))
          }
          if (chooseExecutorFactory.isEmpty) {
            val msg = if (null == labels) {
              "Cannot get default executorFactory. EngineCreation labels: " + GSON.toJson(engineCreationContext.getLabels())
            } else {
              "Cannot get valid executorFactory. EngineCreation labels: " + GSON.toJson(labels)
            }
            error(msg)
            throw new EngineConnPluginErrorException(EngineConnPluginErrorCode.INVALID_LABELS, msg)
          } else {
            chooseExecutorFactory.get.createExecutor(engineCreationContext, engineConn, labels)
          }
        case o =>
          error("Invalid ExecutorFactory " + GSON.toJson(o))
          null
      }
    }
    info(s"Finished create executor ${executor.getId()}")
    executor.init()
    info(s"Finished init executor ${executor.getId()}")
    executor match {
      case labelExecutor: LabelExecutor =>
        runType = getRunTypeFromLables(labelExecutor.getExecutorLabels().asScala.toArray)
      case _ =>
    }
    executors.put(runType, executor)
    if (isDefault) {
      defaultExecutor = executor
    }
    executor
  }

  override def generateId(): Int = idCreator.getAndIncrement()

  override def getExecutorByLabels(labels: Array[Label[_]]): Executor = {
    var runType: String = null
    labels.foreach(l => l match {
      case label: EngineRunTypeLabel =>
        runType = label.getRunType
      case _ =>
    })
    if (null == runType) {
      error("Invalid RunType Label. labels: " + GSON.toJson(labels))
      return null
    }
    if (!executors.containsKey(runType)) {
      val engineConn = EngineConnManager.getEngineConnManager.getEngineConn()
      createExecutor(engineConn.getEngineCreationContext, labels)
    }
    executors.get(runType)
  }

  override def getAllExecutorsMap(): util.Map[String, Executor] = executors

  private def getRunTypeFromLables(labels: Array[Label[_]]): String = {

    labels.foreach {
      case label: EngineRunTypeLabel =>
        return label.getRunType
      case _ =>
    }
    null
  }
}

object ExecutorManager {

  private val executorManager: ExecutorManager = new ExecutorManagerImpl
  Utils.getClassInstance[ExecutorManager](EngineConnExecutorConfiguration.EXECUTOR_MANAGER_SERVICE_CLAZZ.getValue)

  def getInstance(): ExecutorManager = executorManager

}
