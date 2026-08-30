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

package org.apache.linkis.manager.am.selector

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.am.selector.rule.NodeSelectRule
import org.apache.linkis.manager.common.entity.node.{Node, RMNode}
import org.apache.linkis.manager.common.utils.ResourceUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._
import scala.util.Random

@Service
class DefaultNodeSelector extends NodeSelector with Logging {

  @Autowired
  private var ruleList: util.List[NodeSelectRule] = _

  /**
   * Select the most suitable node from a series of nodes through selection rules
   *   1. Rule processing logic, defaults to the last priority
   *   2. After rule filtering, use weighted random selection based on available resources
   *      to avoid always picking the same best node, thereby improving EC distribution balance
   *
   * @param nodes
   * @return
   */
  override def choseNode(nodes: Array[Node]): Option[Node] = {
    if (null == nodes || nodes.isEmpty) {
      None
    } else if (null == ruleList) {
      Some(nodes(0))
    } else {
      var resultNodes = nodes
      Utils.tryAndWarnMsg {
        ruleList.asScala.foreach { rule =>
          resultNodes = rule.ruleFiltering(resultNodes)
        }
      }("Failed to execute select rule")
      if (resultNodes.isEmpty) {
        None
      } else {
        Some(selectNodeWeightedRandom(resultNodes))
      }
    }
  }

  /**
   * Weighted random selection based on node resource availability.
   * Nodes with more remaining resources have higher probability of being selected,
   * which helps distribute EngineConns more evenly across ECMs.
   */
  private def selectNodeWeightedRandom(nodes: Array[Node]): Node = {
    if (nodes.length <= 1) {
      return nodes(0)
    }

    val weights = nodes.map { node =>
      Utils.tryCatch {
        node match {
          case rmNode: RMNode =>
            val nodeResource = rmNode.getNodeResource
            if (nodeResource != null && nodeResource.getLeftResource != null &&
                nodeResource.getMaxResource != null) {
              val rate = ResourceUtils.getLoadInstanceResourceRate(
                nodeResource.getLeftResource,
                nodeResource.getMaxResource
              )
              // Ensure minimum weight of 1.0 to avoid zero-weight nodes being excluded
              Math.max(rate.toDouble, 1.0)
            } else {
              1.0
            }
          case _ => 1.0
        }
      } { case _: Throwable =>
        1.0
      }
    }

    val totalWeight = weights.sum
    if (totalWeight <= 0) {
      return nodes(0)
    }

    val random = Random.nextDouble() * totalWeight
    var cumulativeWeight = 0.0
    for (i <- nodes.indices) {
      cumulativeWeight += weights(i)
      if (cumulativeWeight >= random) {
        return nodes(i)
      }
    }
    nodes(0)
  }

  override def getNodeSelectRules(): Array[NodeSelectRule] = {
    if (null != ruleList) ruleList.asScala.toArray
    else Array.empty[NodeSelectRule]
  }

  override def addNodeSelectRule(nodeSelectRule: NodeSelectRule): Unit = {
    if (null != nodeSelectRule) {
      this.ruleList.add(nodeSelectRule)
    }
  }

}
