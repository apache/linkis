/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.selector

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.am.selector.rule.NodeSelectRule
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._


@Service
class DefaultNodeSelector extends NodeSelector with Logging {

  @Autowired
  private var ruleList: util.List[NodeSelectRule] = _


  /**
    * Select the most suitable node from a series of nodes through selection rules
    * 1. Rule processing logic, defaults to the last priority
    *
    * @param nodes
    * @return
    */
  override def choseNode(nodes: Array[Node]): Option[Node] = {
    if (null == nodes || nodes.isEmpty){
      None
    } else if (null == ruleList ) {
      Some(nodes(0))
    } else {
      var resultNodes = nodes
      Utils.tryAndWarnMsg {
        ruleList.foreach { rule =>
          resultNodes = rule.ruleFiltering(resultNodes)
        }
      }("Failed to execute select rule")
      if (resultNodes.isEmpty) {
        None
      } else {
        Some(resultNodes(0))
      }
    }
  }

  override def getNodeSelectRules(): Array[NodeSelectRule] = {
    if (null != ruleList) ruleList.toList.toArray
    else Array.empty[NodeSelectRule]
  }

  override def addNodeSelectRule(nodeSelectRule: NodeSelectRule): Unit = {
    if (null != nodeSelectRule) {
      this.ruleList.add(nodeSelectRule)
    }
  }
}
