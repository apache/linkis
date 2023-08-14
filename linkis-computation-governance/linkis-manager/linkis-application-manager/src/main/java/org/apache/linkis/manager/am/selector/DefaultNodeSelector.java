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

package org.apache.linkis.manager.am.selector;

import org.apache.linkis.manager.am.selector.rule.NodeSelectRule;
import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultNodeSelector implements NodeSelector {
  private static final Logger logger = LoggerFactory.getLogger(DefaultNodeSelector.class);

  @Autowired private List<NodeSelectRule> ruleList;

  /**
   * Select the most suitable node from a series of nodes through selection rules Rule processing
   * logic, defaults to the last priority
   *
   * @param nodes
   * @return
   */
  @Override
  public Optional<Node> choseNode(Node[] nodes) {
    if (nodes == null || nodes.length == 0) {
      return Optional.empty();
    } else if (ruleList == null) {
      return Optional.of(nodes[0]);
    } else {
      Node[] resultNodes = nodes;
      for (NodeSelectRule rule : ruleList) {
        resultNodes = rule.ruleFiltering(resultNodes);
      }
      if (resultNodes.length == 0) {
        return Optional.empty();
      } else {
        return Optional.of(resultNodes[0]);
      }
    }
  }

  @Override
  public NodeSelectRule[] getNodeSelectRules() {
    return ruleList == null ? new NodeSelectRule[0] : ruleList.toArray(new NodeSelectRule[0]);
  }

  @Override
  public void addNodeSelectRule(NodeSelectRule nodeSelectRule) {
    if (nodeSelectRule != null) {
      ruleList.add(nodeSelectRule);
    }
  }
}
