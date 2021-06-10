package com.webank.wedatasphere.linkis.engineconn.callback.hook

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.callback.service.{EngineConnAfterStartCallback, EngineConnPidCallback}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.engineconn.core.hook.ShutdownHook
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallback
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils


class CallbackEngineConnHook extends EngineConnHook with Logging  {

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    info("Spring is enabled, now try to start SpringBoot.")
    info("<--------------------Start SpringBoot App-------------------->")
    val parser = DWCArgumentsParser.parse(engineCreationContext.getArgs)
    DWCArgumentsParser.setDWCOptionMap(parser.getDWCConfMap)
    val existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    if (!StringUtils.isEmpty(existsExcludePackages))
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key, existsExcludePackages)
    // 加载spring类
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(parser.getSpringConfMap))

    val engineConnPidCallBack = new EngineConnPidCallback(engineCreationContext.getEMInstance)
    Utils.tryAndError(engineConnPidCallBack.callback())
    info("<--------------------SpringBoot App init succeed-------------------->")
  }

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterEngineServerStartFailed(engineCreationContext: EngineCreationContext, throwable: Throwable): Unit = {
    val engineConnAfterStartCallback = new EngineConnAfterStartCallback(engineCreationContext.getEMInstance)
    engineConnAfterStartCallback.callback(EngineConnStatusCallback(Sender.getThisServiceInstance,
      engineCreationContext.getTicketId, NodeStatus.Failed, ExceptionUtils.getRootCauseMessage(throwable)))
    error("EngineConnSever start failed! now exit.", throwable)
    ShutdownHook.getShutdownHook.notifyError(throwable)
  }

  protected def getNodeStatusOfStartSuccess(engineCreationContext: EngineCreationContext,
                                            engineConn: EngineConn): NodeStatus = NodeStatus.Success

  override def afterEngineServerStartSuccess(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    val engineConnAfterStartCallback = new EngineConnAfterStartCallback(engineCreationContext.getEMInstance)
    engineConnAfterStartCallback.callback(EngineConnStatusCallback(Sender.getThisServiceInstance,
      engineCreationContext.getTicketId, getNodeStatusOfStartSuccess(engineCreationContext, engineConn), "success"))
    warn("EngineConnServer start succeed!")
  }

}