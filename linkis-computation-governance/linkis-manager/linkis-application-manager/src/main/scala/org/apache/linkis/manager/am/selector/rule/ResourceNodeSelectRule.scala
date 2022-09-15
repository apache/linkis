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
import org.apache.linkis.manager.common.entity.node.{EMNode, Node, RMNode}
import org.apache.linkis.manager.common.utils.ResourceUtils

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(5)
class ResourceNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes) {
      nodes.sortWith(sortByResource).sortWith(sortByResourceRate)
    } else {
      nodes
    }
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
        Utils.tryCatch {
          val nodeBRM = nodeB.asInstanceOf[RMNode]
          if (null == node.getNodeResource || null == node.getNodeResource.getLeftResource) {
            false
          } else if (
              null == nodeBRM.getNodeResource || null == nodeBRM.getNodeResource.getLeftResource
          ) {
            true
          } else {
            node.getNodeResource.getLeftResource > nodeBRM.getNodeResource.getLeftResource
          }
        } { t: Throwable =>
          logger.warn(s"Failed to Compare resource ${t.getMessage}")
          true
        }
      case _ => false
    }
  }

  private def sortByResourceRate(nodeA: Node, nodeB: Node): Boolean = {
    if (!nodeA.isInstanceOf[EMNode]) {
      return true
    }
    nodeA match {
      case node: RMNode if nodeB.isInstanceOf[RMNode] =>
        Utils.tryCatch {
          val nodeBRM = nodeB.asInstanceOf[RMNode]
          if (null == node.getNodeResource || null == node.getNodeResource.getLeftResource) {
            false
          } else if (
              null == nodeBRM.getNodeResource || null == nodeBRM.getNodeResource.getLeftResource
          ) {
            true
          } else {
            val aRate = ResourceUtils.getLoadInstanceResourceRate(
              node.getNodeResource.getLeftResource,
              node.getNodeResource.getMaxResource
            )
            val bRate = ResourceUtils.getLoadInstanceResourceRate(
              nodeBRM.getNodeResource.getLeftResource,
              nodeBRM.getNodeResource.getMaxResource
            )
            aRate > bRate
          }
        } { t: Throwable =>
          logger.warn(s"Failed to Compare resource ${t.getMessage}")
          true
        }
      case _ => false
    }
  }

}
