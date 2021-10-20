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
 
package org.apache.linkis.engineconn.once.executor.execution

import org.apache.linkis.common.exception.LinkisException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.core.execution.AbstractEngineConnExecution
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.engineconn.once.executor.exception.OnceEngineConnErrorException
import org.apache.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutor}
import org.apache.linkis.manager.label.entity.engine.EngineConnMode._
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}
import org.apache.linkis.scheduler.executer.{AsynReturnExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}

import scala.collection.convert.decorateAsScala._


class OnceEngineConnExecution extends AbstractEngineConnExecution {

  private var onceExecutor: OnceExecutor = _

  override protected def doExecution(executor: Executor,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn): Unit = executor match {
    case onceExecutor: OnceExecutor =>
      this.onceExecutor = onceExecutor
      val response = Utils.tryCatch(onceExecutor.execute(engineCreationContext)) { t =>
        dealException(s"${onceExecutor.getId} execute failed!", t)
        return
      }
      dealResponse(response)
      onceExecutor match {
        case manageableOnceExecutor: ManageableOnceExecutor =>
          manageableOnceExecutor.waitForComplete()
        case _ =>
      }
    case _ => throw new OnceEngineConnErrorException(12560, s"${executor.getId} is not a OnceExecutor.")
  }

  private def dealResponse(resp: ExecuteResponse): Unit = resp match {
    case resp: AsynReturnExecuteResponse =>
      resp.notify(dealResponse)
    case _: SuccessExecuteResponse =>
      onceExecutor.trySucceed()
    case ErrorExecuteResponse(message, t) =>
      if(!onceExecutor.isClosed) {
        dealException(message, t)
      }
  }

  @throws[LinkisException]
  private def dealException(msg: String, t: Throwable): Unit = {
    onceExecutor.tryShutdown()
    onceExecutor.tryFailed()
    t match {
      case t: LinkisException => throw t
      case _ => throw new OnceEngineConnErrorException(12560, msg, t)
    }
  }

  override protected def canExecute(engineCreationContext: EngineCreationContext): Boolean =
    super.canExecute(engineCreationContext) || engineCreationContext.getLabels().asScala.exists {
      case codeLanguageLabel: CodeLanguageLabel =>
        codeLanguageLabel.getCodeType == RunType.JAR.toString
      case _ => false
    }

  override protected def getSupportedEngineConnModes: Array[EngineConnMode] = OnceEngineConnExecution.getSupportedEngineConnModes

  override protected def getReturnEngineConnModes: Array[EngineConnMode] = Array(Once)

  /**
    * Once should between on cluster and computation.
    *
    * @return
    */
  override def getOrder: Int = 100

}
object OnceEngineConnExecution {

  def getSupportedEngineConnModes: Array[EngineConnMode] = Array(Once, Computation_With_Once, Once_With_Cluster)

}