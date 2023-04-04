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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.entity.node.Node

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import scala.util.Random

/**
 * Ten hotspot exclusion rules, random ordering of the previous elements
 */
@Component
@Order(Int.MaxValue)
class HotspotExclusionRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (AMConfiguration.NODE_SELECT_HOTSPOT_EXCLUSION_RULE) {
      randomShuffle(nodes)
    } else {
      nodes
    }
  }

  /**
   * Random sorting if there are more than 10 sorting rules, the first 5 will be randomly sorted if
   * there are no more than 10, the first half will be sorted
   * @param nodes
   * @return
   */
  private def randomShuffle(nodes: Array[Node]): Array[Node] = {
    if (null == nodes || nodes.length < 3) {
      return nodes
    }
    val shuffleSize = if (nodes.length <= 10) {
      nodes.length / 2
    } else {
      5
    }
    val random = new Random()

    for (i <- 0 until shuffleSize) {
      val nextIndex = random.nextInt(shuffleSize)
      val tmpValue = nodes(i)
      nodes(i) = nodes(nextIndex)
      nodes(nextIndex) = tmpValue
    }
    nodes
  }

}
