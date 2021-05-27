package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorException
import com.webank.wedatasphere.linkis.manager.am.service.engine.EngineStopService
import com.webank.wedatasphere.linkis.manager.am.utils.AMUtils
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EngineNode, Node}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineStopRequest, EngineStopResponse, EngineSuicideRequest}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.service.common.pointer.EMNodPointer

/**
  * @author peacewong
  * @date 2020/7/13 20:12
  */
class DefaultEMNodPointer(val node: Node) extends AbstractNodePointer with EMNodPointer {


  /**
    * 与该远程指针关联的node信息
    *
    * @return
    */
  override def getNode(): Node = node

  override def createEngine(engineBuildRequest: EngineConnBuildRequest): EngineNode = {
    info(s"Start to createEngine ask em ${getNode().getServiceInstance}")
    getSender.ask(engineBuildRequest) match {
      case engineNode: EngineNode =>
        info(s"Succeed to createEngine ask em ${getNode().getServiceInstance}, engineNode $engineNode ")
        engineNode
      case _ => throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, s"Failed to ask engine")
    }
  }

  override def stopEngine(engineStopRequest: EngineStopRequest): Unit = {
    Utils.tryAndWarn {
      getSender.ask(engineStopRequest) match {
        case engineStopResponse: EngineStopResponse =>
          if (!engineStopResponse.getStopStatus) {
            info(s"Kill engine : ${engineStopRequest.getServiceInstance.toString} failed, because ${engineStopResponse.getMsg} . Will ask engine to suicide.")
            val engineSuicideRequest = new EngineSuicideRequest(engineStopRequest.getServiceInstance, engineStopRequest.getUser)
            EngineStopService.askEngineToSuicide(engineSuicideRequest)
          } else {
            info(s"Succeed to kill engine ${engineStopRequest.getServiceInstance.toString}.")
          }
        case o: AnyRef =>
          warn(s"Ask em : ${getNode().getServiceInstance.toString} to kill engine : ${engineStopRequest.getServiceInstance.toString} failed, response is : ${AMUtils.GSON.toJson(o)}. ")
      }
    }
  }


}
