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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.hook

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


class ComputationEngineConnHook extends EngineConnHook with Logging {

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    info("<----------Start Spring app---------->")
    val parser = DWCArgumentsParser.parse(engineCreationContext.getArgs)
    DWCArgumentsParser.setDWCOptionMap(parser.getDWCConfMap)
    val existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    if (!StringUtils.isEmpty(existsExcludePackages))
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key, existsExcludePackages)
    // 加载spring类
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(parser.getSpringConfMap))

    val engineConnPidCallBack = new EngineConnPidCallback(engineCreationContext.getEMInstance)
    Utils.tryAndError(engineConnPidCallBack.callback())
    info("<----------Spring app init success---------->")
  }


  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterEngineServerStartFailed(engineCreationContext: EngineCreationContext, throwable: Throwable): Unit = {

    val engineConnAfterStartCallback = new EngineConnAfterStartCallback(engineCreationContext.getEMInstance)
    engineConnAfterStartCallback.callback(EngineConnStatusCallback(Sender.getThisServiceInstance,
      engineCreationContext.getTicketId, NodeStatus.ShuttingDown, ExceptionUtils.getRootCauseMessage(throwable)))
    error("init engineConn failed! now exit", throwable)
    ShutdownHook.getShutdownHook.notifyError(throwable)
  }

  override def afterEngineServerStartSuccess(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    val engineConnAfterStartCallback = new EngineConnAfterStartCallback(engineCreationContext.getEMInstance)
    engineConnAfterStartCallback.callback(EngineConnStatusCallback(Sender.getThisServiceInstance,
      engineCreationContext.getTicketId, NodeStatus.Unlock, "success"))
    warn("init engineConn success!")
  }


}
