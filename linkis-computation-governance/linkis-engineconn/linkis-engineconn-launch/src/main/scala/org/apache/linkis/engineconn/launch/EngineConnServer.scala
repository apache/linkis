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
 
package org.apache.linkis.engineconn.launch

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.{DefaultEngineCreationContext, EngineCreationContext}
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.execution.{AbstractEngineConnExecution, EngineConnExecution}
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.core.util.EngineConnUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.exception.engineconn.{EngineConnExecutorErrorCode, EngineConnExecutorErrorException}
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.label.builder.factory.{LabelBuilderFactory, LabelBuilderFactoryContext}
import org.apache.linkis.manager.label.entity.Label
import org.apache.commons.lang.exception.ExceptionUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer


object EngineConnServer extends Logging {


  private val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext
  private val labelBuilderFactory: LabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory


  def main(args: Array[String]): Unit = {
    info("<<---------------------EngineConnServer Start --------------------->>")

    try {
      // 1. 封装EngineCreationContext
      init(args)
      val isTestMode = Configuration.IS_TEST_MODE.getValue(engineCreationContext.getOptions)
      if(isTestMode) {
        info(s"Step into test mode, pause 30s if debug is required. If you want to disable test mode, please set ${Configuration.IS_TEST_MODE.key} = false.")
        Utils.sleepQuietly(30000)
      }
      info("Finished to create EngineCreationContext, EngineCreationContext content: " + EngineConnUtils.GSON.toJson(engineCreationContext))
      EngineConnHook.getEngineConnHooks.foreach(_.beforeCreateEngineConn(getEngineCreationContext))
      info("Finished to execute hook of beforeCreateEngineConn.")
      //2. 创建EngineConn
      val engineConn = getEngineConnManager.createEngineConn(getEngineCreationContext)
      info(s"Finished to create ${engineConn.getEngineConnType}EngineConn.")
      EngineConnHook.getEngineConnHooks.foreach(_.beforeExecutionExecute(getEngineCreationContext, engineConn))
      info("Finished to execute all hooks of beforeExecutionExecute.")
      //3. 注册的executions 执行
      Utils.tryThrow(executeEngineConn(engineConn)){ t =>
        error(s"Init executors error. Reason: ${ExceptionUtils.getRootCauseMessage(t)}", t)
        throw new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INIT_EXECUTOR_FAILED, "Init executors failed. ", t)
      }
      EngineConnObject.setReady()
      info("Finished to execute executions.")
      EngineConnHook.getEngineConnHooks.foreach(_.afterExecutionExecute(getEngineCreationContext, engineConn))
      info("Finished to execute hook of afterExecutionExecute")
      EngineConnHook.getEngineConnHooks.foreach(_.afterEngineServerStartSuccess(getEngineCreationContext, engineConn))
    } catch {
      case t: Throwable =>
        error("EngineConnServer Start Failed", t)
        EngineConnHook.getEngineConnHooks.foreach(_.afterEngineServerStartFailed(getEngineCreationContext, t))
        System.exit(1)
    }

    //4. 等待Executions执行完毕
    ShutdownHook.getShutdownHook.waitForStopOrError()
    info("<<---------------------EngineConnServer Exit --------------------->>")
    System.exit(ShutdownHook.getShutdownHook.getExitCode())
  }

  /**
    *
    * @param args main函数入参
    */
  private def init(args: Array[String]): Unit = {
    val arguments = EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToObj(args)
    val engineConf = arguments.getEngineConnConfMap
    this.engineCreationContext.setUser(engineConf.getOrElse("user", Utils.getJvmUser))
    this.engineCreationContext.setTicketId(engineConf.getOrElse("ticketId", ""))
    val host = CommonVars(Environment.ECM_HOST.toString, "127.0.0.1").getValue
    val port = CommonVars(Environment.ECM_PORT.toString, "80").getValue
    this.engineCreationContext.setEMInstance(ServiceInstance(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue, s"$host:$port"))
    val labels = new ArrayBuffer[Label[_]]
    val labelArgs = engineConf.filter(_._1.startsWith(EngineConnArgumentsParser.LABEL_PREFIX))
    if (labelArgs.nonEmpty) {
      labelArgs.foreach { case (key, value) =>
        labels += labelBuilderFactory.createLabel[Label[_]](key.replace(EngineConnArgumentsParser.LABEL_PREFIX, ""), value)
      }
      engineCreationContext.setLabels(labels.toList)
    }
    val jMap = new java.util.HashMap[String, String](engineConf.size)
    jMap.putAll(engineConf)
    this.engineCreationContext.setOptions(jMap)
    this.engineCreationContext.setArgs(args)
    EngineConnObject.setEngineCreationContext(this.engineCreationContext)
    info("Finished to init engineCreationContext: " + EngineConnUtils.GSON.toJson(engineCreationContext))
  }

  private def executeEngineConn(engineConn: EngineConn): Unit = {
    EngineConnExecution.getEngineConnExecutions.foreach{
      case execution: AbstractEngineConnExecution =>
        info(s"Ready to execute ${execution.getClass.getSimpleName}.")
        execution.execute(getEngineCreationContext, engineConn)
        if(execution.returnAfterMeExecuted(getEngineCreationContext, engineConn)) return
      case execution =>
        info(s"Ready to execute ${execution.getClass.getSimpleName}.")
        execution.execute(getEngineCreationContext, engineConn)}
  }

  def getEngineCreationContext: EngineCreationContext = this.engineCreationContext

  private def getEngineConnManager: EngineConnManager = EngineConnManager.getEngineConnManager

}
