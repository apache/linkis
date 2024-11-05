/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.am.selector.rule

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.metrics.NodeOverLoadInfo
import org.apache.linkis.manager.common.entity.node.{AMNode, Node}

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(3)
class OverLoadNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes) {
      nodes.sortWith(sortByOverload)
    } else {
      nodes
    }
  }

  /**
   * sort by sortByOverload
   *
   * @param nodeA
   * @param nodeB
   * @return
   */
  private def sortByOverload(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case node: AMNode if nodeB.isInstanceOf[AMNode] =>
        Utils.tryCatch(
          getOverload(node.getNodeOverLoadInfo) < getOverload(
            nodeB.asInstanceOf[AMNode].getNodeOverLoadInfo
          )
        ) { t: Throwable =>
          logger.warn("Failed to Compare resource ", t)
          true
        }

      case _ => false
    }
  }

  private def getOverload(overloadInfo: NodeOverLoadInfo): Float = {
    if (overloadInfo == null) 0f else overloadInfo.getUsedMemory * 1f / overloadInfo.getMaxMemory
  }

}
