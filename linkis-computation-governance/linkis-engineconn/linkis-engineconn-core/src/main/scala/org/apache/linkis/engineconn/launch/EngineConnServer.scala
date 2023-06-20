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

package org.apache.linkis.engineconn.launch

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.conf.EngineConnConstant
import org.apache.linkis.engineconn.common.creation.{
  DefaultEngineCreationContext,
  EngineCreationContext
}
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.execution.{
  AbstractEngineConnExecution,
  EngineConnExecution
}
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.core.util.EngineConnUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.exception.engineconn.{
  EngineConnExecutorErrorCode,
  EngineConnExecutorErrorException
}
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineConnMode

import org.apache.commons.lang3.exception.ExceptionUtils

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

object EngineConnServer extends Logging {

  private val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext

  private val labelBuilderFactory: LabelBuilderFactory =
    LabelBuilderFactoryContext.getLabelBuilderFactory

  private var onceMode: Boolean = false

  def main(args: Array[String]): Unit = {
    logger.info("<<---------------------EngineConnServer Start --------------------->>")

    try {
      // 1. build EngineCreationContext
      init(args)
      val isTestMode = Configuration.IS_TEST_MODE.getValue(engineCreationContext.getOptions)
      if (isTestMode) {
        logger.info(
          s"Step into test mode, pause 30s if debug is required. If you want to disable test mode, please set ${Configuration.IS_TEST_MODE.key} = false."
        )
        Utils.sleepQuietly(30000)
      }
      logger.info(
        "Finished to create EngineCreationContext, EngineCreationContext content: " + EngineConnUtils.GSON
          .toJson(engineCreationContext)
      )
      val ecHooks = EngineConnHook.getEngineConnHooks(onceMode)
      ecHooks.foreach(_.beforeCreateEngineConn(getEngineCreationContext))
      logger.info("Finished to execute hook of beforeCreateEngineConn.")
      // 2. create EngineConn
      val engineConn = getEngineConnManager.createEngineConn(getEngineCreationContext)
      logger.info(s"Finished to create ${engineConn.getEngineConnType} EngineConn.")
      ecHooks.foreach(_.beforeExecutionExecute(getEngineCreationContext, engineConn))
      logger.info("Finished to execute all hooks of beforeExecutionExecute.")
      // 3. register executions
      Utils.tryThrow(executeEngineConn(engineConn)) { t =>
        logger.error(s"Init executors error. Reason: ${ExceptionUtils.getRootCauseMessage(t)}", t)
        throw new EngineConnExecutorErrorException(
          EngineConnExecutorErrorCode.INIT_EXECUTOR_FAILED,
          "Init executors failed. ",
          t
        )
      }
      EngineConnObject.setReady()
      logger.info("Finished to execute executions.")
      ecHooks.foreach(_.afterExecutionExecute(getEngineCreationContext, engineConn))
      logger.info("Finished to execute hook of afterExecutionExecute")
      ecHooks.foreach(_.afterEngineServerStartSuccess(getEngineCreationContext, engineConn))
    } catch {
      case t: Throwable =>
        logger.error("EngineConnServer Start Failed.", t)
        val ecHooks = EngineConnHook.getEngineConnHooks(onceMode)
        ecHooks.foreach(_.afterEngineServerStartFailed(getEngineCreationContext, t))
        System.exit(1)
    }

    // 4. wait Executions execute
    ShutdownHook.getShutdownHook.waitForStopOrError()
    logger.info("<<---------------------EngineConnServer Exit --------------------->>")
    System.exit(ShutdownHook.getShutdownHook.getExitCode())
  }

  private def init(args: Array[String]): Unit = {
    val arguments = EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToObj(args)
    val engineConf = arguments.getEngineConnConfMap
    this.engineCreationContext.setUser(engineConf.getOrElse("user", Utils.getJvmUser))
    this.engineCreationContext.setTicketId(engineConf.getOrElse(ECConstants.EC_TICKET_ID_KEY, ""))
    val host = CommonVars(Environment.ECM_HOST.toString, "127.0.0.1").getValue
    val port = CommonVars(Environment.ECM_PORT.toString, "80").getValue
    this.engineCreationContext.setEMInstance(
      ServiceInstance(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue, s"$host:$port")
    )

    val labels = new ArrayBuffer[Label[_]]
    val labelArgs = engineConf.filter(_._1.startsWith(EngineConnArgumentsParser.LABEL_PREFIX))
    if (labelArgs.nonEmpty) {
      labelArgs.foreach { case (key, value) =>
        val realKey = key.substring(EngineConnArgumentsParser.LABEL_PREFIX.length)
        if ("engineConnMode".equals(realKey)) {
          onceMode = EngineConnMode.isOnceMode(value)
        }
        labels += labelBuilderFactory.createLabel[Label[_]](realKey, value)
      }
      engineCreationContext.setLabels(labels.toList.asJava)
    }
    val jMap = new java.util.HashMap[String, String](engineConf.size)
    jMap.putAll(engineConf.asJava)
    this.engineCreationContext.setOptions(jMap)
    this.engineCreationContext.setArgs(args)
    EngineConnObject.setEngineCreationContext(this.engineCreationContext)
    logger.info(
      "Finished to init engineCreationContext: " + EngineConnUtils.GSON
        .toJson(engineCreationContext)
    )
  }

  private def executeEngineConn(engineConn: EngineConn): Unit = {
    EngineConnExecution.getEngineConnExecutions.foreach {
      case execution: AbstractEngineConnExecution =>
        logger.info(s"Ready to execute ${execution.getClass.getSimpleName}.")
        execution.execute(getEngineCreationContext, engineConn)
        if (execution.returnAfterMeExecuted(getEngineCreationContext, engineConn)) return
      case execution =>
        logger.info(s"Ready to execute ${execution.getClass.getSimpleName}.")
        execution.execute(getEngineCreationContext, engineConn)
    }
  }

  def isOnceMode: Boolean = this.onceMode

  def getEngineCreationContext: EngineCreationContext = this.engineCreationContext

  private def getEngineConnManager: EngineConnManager = EngineConnManager.getEngineConnManager

}
