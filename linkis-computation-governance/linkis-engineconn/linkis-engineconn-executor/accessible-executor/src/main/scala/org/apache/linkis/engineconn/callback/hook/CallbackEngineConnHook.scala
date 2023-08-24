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

package org.apache.linkis.engineconn.callback.hook

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.DWCArgumentsParser
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.callback.service.{
  EngineConnAfterStartCallback,
  EngineConnIdentifierCallback
}
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.EngineConnStatusCallback
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import scala.collection.mutable

class CallbackEngineConnHook extends EngineConnHook with Logging {

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    logger.info("Spring is enabled, now try to start SpringBoot.")
    logger.info("<--------------------Start SpringBoot App-------------------->")
    val parser = DWCArgumentsParser.parse(engineCreationContext.getArgs)
    DWCArgumentsParser.setDWCOptionMap(parser.getDWCConfMap)
    val existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    if (StringUtils.isNotBlank(existsExcludePackages)) {
      DataWorkCloudApplication.setProperty(
        ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key,
        existsExcludePackages
      )
    }
    // 加载spring类
    val map = new mutable.HashMap[String, String]()
    val newMap = map.++(parser.getSpringConfMap)
    newMap.put("spring.mvc.servlet.path", ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue)
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(newMap.toMap))

    logger.info("<--------------------SpringBoot App init succeed-------------------->")
  }

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    val engineConnIdentifierCallback = new EngineConnIdentifierCallback()
    Utils.tryAndError(engineConnIdentifierCallback.callback())
  }

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {}

  override def afterEngineServerStartFailed(
      engineCreationContext: EngineCreationContext,
      throwable: Throwable
  ): Unit = {
    val engineConnAfterStartCallback = new EngineConnAfterStartCallback
    val prefixMsg = Sender.getThisServiceInstance + s": log dir: ${EngineConnConf.getLogDir},"
    Utils.tryAndError(
      engineConnAfterStartCallback.callback(
        new EngineConnStatusCallback(
          Sender.getThisServiceInstance,
          engineCreationContext.getTicketId,
          NodeStatus.Failed,
          prefixMsg + ExceptionUtils.getStackTrace(throwable)
        )
      )
    )
    logger.error("EngineConnSever start failed! now exit.", throwable)
    ShutdownHook.getShutdownHook.notifyError(throwable)
  }

  protected def getNodeStatusOfStartSuccess(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): NodeStatus = {
    ExecutorManager.getInstance.getReportExecutor match {
      case executor: AccessibleExecutor =>
        if (
            executor.getStatus == NodeStatus.ShuttingDown || executor.getStatus == NodeStatus.Failed
        ) {
          logger.info(
            "The status of EngineConn is {}, and the actual status will be reported",
            executor.getStatus
          )
          executor.getStatus
        } else {
          NodeStatus.Unlock
        }
      case _ => NodeStatus.Unlock
    }
  }

  override def afterEngineServerStartSuccess(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    val engineConnAfterStartCallback = new EngineConnAfterStartCallback
    Utils.tryAndError(
      engineConnAfterStartCallback.callback(
        new EngineConnStatusCallback(
          Sender.getThisServiceInstance,
          engineCreationContext.getTicketId,
          getNodeStatusOfStartSuccess(engineCreationContext, engineConn),
          "success"
        )
      )
    )
    logger.warn("EngineConnServer start succeed!")
  }

}
