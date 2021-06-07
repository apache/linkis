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
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.core.util.EngineConnUtils
import com.webank.wedatasphere.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, LabelExecutor, SensibleExecutor}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{CodeLanguageLabelExecutorFactory, ExecutorFactory, LabelExecutorFactory, MultiExecutorEngineConnFactory, SingleExecutorEngineConnFactory, SingleLabelExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.{EngineConnPluginErrorCode, EngineConnPluginErrorException}
import com.webank.wedatasphere.linkis.manager.label.entity.Label

import scala.collection.JavaConverters._

trait ExecutorManager {

  def getExecutor(id: String): Executor

  def removeExecutor(id: String): Executor

  def getExecutors: Array[Executor]

  def generateExecutorId(): Int

  def getReportExecutor: Executor

}

trait LabelExecutorManager extends ExecutorManager {

  def getExecutorByLabels(labels: Array[Label[_]]): LabelExecutor

  def removeExecutor(labels: Array[Label[_]]): LabelExecutor

}

class LabelExecutorManagerImpl extends LabelExecutorManager with Logging {

  private lazy val executors: util.Map[String, LabelExecutor] = new ConcurrentHashMap[String, LabelExecutor](2)
  protected val GSON = EngineConnUtils.GSON
  private val idCreator = new AtomicInteger()

  protected lazy val engineConn: EngineConn = EngineConnManager.getEngineConnManager.getEngineConn
  protected val engineConnPlugin: EngineConnPlugin = EngineConnObject.getEngineConnPlugin

  protected val (factories, defaultFactory): (Array[ExecutorFactory], ExecutorFactory) = engineConnPlugin.getEngineConnFactory match {
    case engineConnFactory: SingleExecutorEngineConnFactory =>
      (Array.empty, engineConnFactory)
    case engineConnFactory: SingleLabelExecutorEngineConnFactory =>
      (Array(engineConnFactory), engineConnFactory)
    case engineConnFactory: MultiExecutorEngineConnFactory =>
      (engineConnFactory.getExecutorFactories, engineConnFactory.getDefaultExecutorFactory)
    case engineConnFactory =>
      val errorMsg = "Not supported ExecutorFactory " + engineConnFactory.getClass.getSimpleName
      error(errorMsg)
      throw new EngineConnPluginErrorException(20011, errorMsg)
  }

  protected def tryCreateExecutor(engineCreationContext: EngineCreationContext,
                                  labels: Array[Label[_]]): LabelExecutor = {
    val labelExecutor = Option(labels).flatMap(_ => factories.find {
      case labelExecutorFactory: LabelExecutorFactory => labelExecutorFactory.canCreate(labels)
      case _ => false
    }.map {
      case labelExecutorFactory: LabelExecutorFactory => labelExecutorFactory.createExecutor(engineCreationContext, engineConn, labels)
    }).getOrElse(defaultFactory.createExecutor(engineCreationContext, engineConn).asInstanceOf[LabelExecutor])
    info(s"Finished create executor ${labelExecutor.getId}.")
    labelExecutor.init()
    info(s"Finished init executor ${labelExecutor.getId}.")
    labelExecutor
  }

  protected def createExecutor(engineCreationContext: EngineCreationContext): LabelExecutor = {
    defaultFactory match {
      case labelExecutorFactory: CodeLanguageLabelExecutorFactory =>
        createExecutor(engineCreationContext, Array[Label[_]](labelExecutorFactory.getDefaultCodeLanguageLabel))
      case _ =>
        val executor = tryCreateExecutor(engineCreationContext, null)
        executors.put(executor.getId, executor)
        executor
    }

  }

  protected def getLabelKey(labels: Array[Label[_]]): String = labels.map(_.getStringValue).mkString("&")

  protected def createExecutor(engineCreationContext: EngineCreationContext,
                              labels: Array[Label[_]]): LabelExecutor = {
    if(null == labels || labels.isEmpty) return createExecutor(engineCreationContext)
    val labelKey = getLabelKey(labels)
    if (null == labelKey) {
      val msg = "Cannot get label key. labels : " + GSON.toJson(labels)
      throw new EngineConnPluginErrorException(EngineConnPluginErrorCode.INVALID_LABELS, msg)
    }
    val executor = tryCreateExecutor(engineCreationContext, labels)
    executors.put(labelKey, executor)
    executor
  }

  override def generateExecutorId(): Int = idCreator.getAndIncrement()

  override def getExecutorByLabels(labels: Array[Label[_]]): LabelExecutor = {
    val labelKey = getLabelKey(labels)
    if (null == labelKey) return null
    if (!executors.containsKey(labelKey)) {
      createExecutor(engineConn.getEngineCreationContext, labels)
    }
    executors.get(labelKey)
  }

  override def getExecutor(id: String): Executor = executors.values().asScala.find(_.getId == id).orNull

  override def getExecutors: Array[Executor] = executors.values().asScala.toArray

  override def removeExecutor(labels: Array[Label[_]]): LabelExecutor = {
    val labelKey = getLabelKey(labels)
    if (labelKey != null && executors.containsKey(labelKey)) executors.remove(labelKey)
    else null
  }

  override def removeExecutor(id: String): Executor = executors.asScala.find(_._2.getId == id).map{
    case (k, _) => executors.remove(k)
  }.orNull

  override def getReportExecutor: Executor = if(getExecutors.isEmpty) createExecutor(engineConn.getEngineCreationContext)
  else getExecutors.maxBy {
    case executor: SensibleExecutor => executor.getLastActivityTime
    case executor: Executor => executor.getId.hashCode
  }
}

object ExecutorManager {

  private var executorManager: ExecutorManager = _

  private def init(): Unit = {
    executorManager = Utils.getClassInstance[ExecutorManager](EngineConnExecutorConfiguration.EXECUTOR_MANAGER_CLASS.acquireNew)
  }

  def getInstance: ExecutorManager = {
    if(executorManager == null) synchronized {
      if(executorManager == null) init()
    }
    executorManager
  }

}
