package com.webank.wedatasphere.linkis.manager.service.common.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineStopRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest

/**
  * @author peacewong
  * @date 2020/7/13 18:03
  */
trait EMNodPointer extends NodePointer {

  def createEngine(engineBuildRequest: EngineConnBuildRequest): EngineNode

  def stopEngine(engineStopRequest: EngineStopRequest): Unit

}
