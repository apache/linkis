package com.webank.wedatasphere.linkis.manager.service.common.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}


trait NodePointerBuilder {

  def buildEMNodePointer(node: EMNode): EMNodPointer

  def buildEngineNodePointer(node: EngineNode): EngineNodePointer

}
