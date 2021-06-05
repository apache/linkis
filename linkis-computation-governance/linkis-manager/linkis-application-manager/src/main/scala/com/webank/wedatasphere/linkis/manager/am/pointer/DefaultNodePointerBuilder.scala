package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.service.common.pointer.{EMNodPointer, EngineNodePointer, NodePointerBuilder}
import org.springframework.stereotype.Component


@Component
class DefaultNodePointerBuilder extends NodePointerBuilder {


  override def buildEMNodePointer(node: EMNode): EMNodPointer = {
    new DefaultEMNodPointer(node)
  }

  override def buildEngineNodePointer(node: EngineNode): EngineNodePointer = {
    new DefaultEngineNodPointer(node)
  }

}
