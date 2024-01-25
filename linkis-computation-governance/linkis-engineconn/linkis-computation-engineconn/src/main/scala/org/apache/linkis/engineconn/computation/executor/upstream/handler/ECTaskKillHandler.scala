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

package org.apache.linkis.engineconn.computation.executor.upstream.handler

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.upstream.wrapper.{
  ConnectionInfoWrapper,
  ECTaskEntranceConnectionWrapper
}
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel

class ECTaskKillHandler extends MonitorHandler with Logging {

  override def handle(request: MonitorHandlerRequest): Unit = {
    if (request == null) {
      logger.error("illegal input for handler: null")
    } else {
      request match {
        case _: ECTaskKillHandlerRequest =>
          val toBeKilled = request.asInstanceOf[ECTaskKillHandlerRequest].getData
          if (toBeKilled != null && toBeKilled.size() != 0) {
            val elements = toBeKilled.iterator
            while (elements.hasNext) {
              val element = elements.next
              Utils.tryCatch {
                logger.error(
                  s"ERROR: entrance : ${element.getUpstreamConnection().getUpstreamServiceInstanceName()} lose connect, will kill job : ${element.getKey()}"
                )
                doKill(element)
              } { t =>
                logger.error("Failed to kill job: " + element.getKey, t)
              }
            }
          }
        case _ => logger.error("illegal input for handler: " + request.getClass.getCanonicalName)
      }
    }
  }

  private def doKill(wrapper: ConnectionInfoWrapper): Unit = {
    if (wrapper != null) {
      wrapper match {
        case eCTaskEntranceConnectionWrapper: ECTaskEntranceConnectionWrapper =>
          if (
              eCTaskEntranceConnectionWrapper.getExecutor == null || eCTaskEntranceConnectionWrapper.getEngineConnTask == null
          ) {
            logger.error("Failed to kill job, executor or engineConnTask in wrapper is null")
          } else {
            eCTaskEntranceConnectionWrapper.getExecutor.killTask(
              eCTaskEntranceConnectionWrapper.getEngineConnTask.getTaskId
            )
            if (
                eCTaskEntranceConnectionWrapper.getEngineConnTask.getLables.exists(
                  _.isInstanceOf[ExecuteOnceLabel]
                )
            ) {
              logger.warn(
                "upstream monitor tries to shutdown engineConn because executeOnce-label was found"
              )
              ExecutorManager.getInstance.getReportExecutor.tryShutdown()
            }
          }
        case _ => logger.error("invalid data-type: " + wrapper.getClass.getCanonicalName)
      }
    } else {
      logger.error("wrapper is null")
    }
  }

}
