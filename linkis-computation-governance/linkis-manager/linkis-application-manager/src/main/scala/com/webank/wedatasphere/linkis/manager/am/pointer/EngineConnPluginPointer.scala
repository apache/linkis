package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.EngineResourceRequest


trait EngineConnPluginPointer {

  def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource

}
