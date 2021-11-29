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
 
package org.apache.linkis.engineconn.computation.executor.creation

import java.util.concurrent.TimeUnit

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, RetryHandler, Utils}
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.executor.{ExecutorManager, LabelExecutorManager, LabelExecutorManagerImpl}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel

import scala.concurrent.duration.Duration


trait ComputationExecutorManager extends LabelExecutorManager {

  def getDefaultExecutor: ComputationExecutor

  //override def getReportExecutor: ComputationExecutor

}

object ComputationExecutorManager {

  private lazy val executorManager = ExecutorManager.getInstance match {
    case manager: ComputationExecutorManager => manager
  }

  def getInstance: ComputationExecutorManager = executorManager

}

class ComputationExecutorManagerImpl extends LabelExecutorManagerImpl with ComputationExecutorManager with Logging {

  private var defaultExecutor: ComputationExecutor = _

  override def getDefaultExecutor: ComputationExecutor = {
    if(defaultExecutor != null) return defaultExecutor
    synchronized {
      if (null == defaultExecutor || defaultExecutor.isClosed) {
        if (null == EngineConnManager.getEngineConnManager.getEngineConn) {
          Utils.waitUntil(() => null != EngineConnManager.getEngineConnManager.getEngineConn, Duration.apply(EngineConnConf.ENGINE_CONN_CREATION_WAIT_TIME.getValue.toLong, TimeUnit.MILLISECONDS))
          error(s"Create default executor failed, engineConn not ready after ${EngineConnConf.ENGINE_CONN_CREATION_WAIT_TIME.getValue.toString}.")
          return null
        }
        val retryHandler = new RetryHandler {}
        retryHandler.addRetryException(classOf[ErrorException]) // Linkis exception will retry.
        defaultExecutor = retryHandler.retry(createExecutor(engineConn.getEngineCreationContext), "Create default executor") match {
          case computationExecutor: ComputationExecutor => computationExecutor
        }
      }
      defaultExecutor
    }
  }

  /*override def getReportExecutor: ComputationExecutor = if(getExecutors.isEmpty) getDefaultExecutor
  else getExecutors.maxBy {
    case computationExecutor: ComputationExecutor => computationExecutor.getStatus.ordinal()
  }.asInstanceOf[ComputationExecutor]*/

  override protected def getLabelKey(labels: Array[Label[_]]): String = {
    labels.foreach {
      case label: CodeLanguageLabel =>
        return label.getCodeType
      case _ =>
    }
    error("Cannot get label key. labels : " + GSON.toJson(labels))
    null
  }
}