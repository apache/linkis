package com.webank.wedatasphere.linkis.engineconn.launch

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.{DefaultEngineCreationContext, EngineCreationContext}
import com.webank.wedatasphere.linkis.engineconn.common.execution.EngineExecution
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.core.util.EngineConnUtils
import com.webank.wedatasphere.linkis.engineconn.core.hook.ShutdownHook
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.governance.common.utils.EngineConnArgumentsParser
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.Environment
import com.webank.wedatasphere.linkis.manager.label.builder.factory.{LabelBuilderFactory, StdLabelBuilderFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.Label

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer


object EngineConnServer extends Logging {


  private val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext
  private val labelBuilderFactory: LabelBuilderFactory = new StdLabelBuilderFactory

  var isReady = false

  def main(args: Array[String]): Unit = {
    info("<<---------------------EngineConnServer Start --------------------->>")

    Utils.tryCatch {
      // 1. 封装EngineCreationContext
      init(args)
      info("Finished to create EngineCreationContext")
      EngineConnHook.getEngineConnHooks.foreach(_.beforeCreateEngineConn(getEngineCreationContext))
      info("Finished to execute hook of beforeCreateEngineConn")
      //2. 创建EngineConn
      val engineConn = getEngineConnManager.createEngineConn(getEngineCreationContext)
      info(s"Finished to create engineConn that type is ${engineConn.getEngineType()}")
      EngineConnHook.getEngineConnHooks.foreach(_.beforeExecutionExecute(getEngineCreationContext, engineConn))
      info("Finished to execute hook of beforeExecutionExecute")
      //3. 注册的executions 执行
      getEngineExecutions.foreach(_.execute(getEngineCreationContext, engineConn))
      EngineConnObject.setReady
      info("Finished to execute executions")
      EngineConnHook.getEngineConnHooks.foreach(_.afterExecutionExecute(getEngineCreationContext, engineConn))
      info("Finished to execute hook of afterExecutionExecute")
      EngineConnHook.getEngineConnHooks.foreach(_.afterEngineServerStartSuccess(getEngineCreationContext, engineConn))
    } { t =>
      EngineConnHook.getEngineConnHooks.foreach(_.afterEngineServerStartFailed(getEngineCreationContext, t))
    }

    //4. 等待Executions执行完毕
    ShutdownHook.getShutdownHook.waitForStopOrError()
    info("<<---------------------EngineConnServer Exit --------------------->>")
    System.exit(ShutdownHook.getShutdownHook.getExitCode())
  }

  /**
    *
    * @param args
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
    info("Finished to init engineCreationContext" + EngineConnUtils.GSON.toJson(engineCreationContext))
  }

  def getEngineCreationContext: EngineCreationContext = this.engineCreationContext

  private def getEngineConnManager: EngineConnManager = EngineConnManager.getEngineConnManager

  private def getEngineExecutions: Array[EngineExecution] = EngineExecution.getEngineExecutions

  private def getShutdownHook: ShutdownHook = ShutdownHook.getShutdownHook

}
