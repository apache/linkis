package com.webank.wedatasphere.linkis.manager.service.common.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}

/**
  * @author peacewong
  * @date 2020/7/13 20:01
  */
trait NodePointerBuilder {

  def buildEMNodePointer(node: EMNode): EMNodPointer

  def buildEngineNodePointer(node: EngineNode): EngineNodePointer

}
