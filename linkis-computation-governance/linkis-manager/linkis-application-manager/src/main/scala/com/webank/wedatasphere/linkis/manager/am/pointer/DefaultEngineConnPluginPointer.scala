package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorException
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.EngineResourceRequest
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/9/17 20:17
  */
@Component
class DefaultEngineConnPluginPointer extends EngineConnPluginPointer {

  private def getEngineConnPluginSender: Sender = Sender.getSender(AMConfiguration.ENGINECONN_SPRING_APPLICATION_NAME.getValue)

  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    getEngineConnPluginSender.ask(engineResourceRequest) match {
      case nodeResource: NodeResource =>
        nodeResource
      case _ =>
        throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, s"Failed to create engineResource")
    }
  }
}
