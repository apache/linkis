package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.EngineResourceRequest

/**
  * @author peacewong
  * @date 2020/9/17 20:15
  */
trait EngineConnPluginPointer {

  def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource

}
