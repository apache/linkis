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

package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.node.{Node, RMNode}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * @date 2020/7/4 22:54
  */
@Component
@Order(5)
class ResourceNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes)
      nodes.sortWith(sortByResource)
    else
      nodes
  }

  /**
    * sort by label score
    *
    * @param nodeA
    * @param nodeB
    * @return
    */
  private def sortByResource(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case node: RMNode if nodeB.isInstanceOf[RMNode] =>
        Utils.tryCatch(node.getNodeResource.getLeftResource > nodeB.asInstanceOf[RMNode].getNodeResource.getLeftResource) {
          t: Throwable =>
            warn("Failed to Compare resource ", t)
            true
        }
      case _ => false
    }
  }


}
