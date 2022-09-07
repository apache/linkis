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
import org.apache.linkis.manager.common.entity.node.{Node, ScoreServiceInstance}

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(0)
class ScoreNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes) nodes.sortWith(sortByScore)
    else nodes
  }

  /**
   * sort by label score
   *
   * @param nodeA
   * @param nodeB
   * @return
   */
  private def sortByScore(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case instance: ScoreServiceInstance if nodeB.isInstanceOf[ScoreServiceInstance] =>
        Utils.tryCatch(instance.getScore > nodeB.asInstanceOf[ScoreServiceInstance].getScore) {
          t: Throwable =>
            logger.warn("Failed to Compare resource ", t)
            true
        }

      case _ => false
    }
  }

}
