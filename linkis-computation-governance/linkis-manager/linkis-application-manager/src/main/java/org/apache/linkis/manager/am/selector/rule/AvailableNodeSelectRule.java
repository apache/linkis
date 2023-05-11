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

package org.apache.linkis.manager.am.selector.rule;

import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.AMNode;
import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(2)
public class AvailableNodeSelectRule implements NodeSelectRule {

  private Logger logger = LoggerFactory.getLogger(AvailableNodeSelectRule.class);

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (null != nodes) {
      return Arrays.stream(nodes)
          .filter(
              node -> {
                if (node instanceof AMNode) {
                  AMNode amNode = (AMNode) node;
                  if (!NodeStatus.isLocked(amNode.getNodeStatus())
                      && NodeStatus.isAvailable(amNode.getNodeStatus())) {
                    return null != amNode.getNodeHealthyInfo()
                        && null != amNode.getNodeHealthyInfo().getNodeHealthy()
                        && NodeHealthy.isAvailable(amNode.getNodeHealthyInfo().getNodeHealthy());
                  } else {
                    logger.info(
                        String.format(
                            "engineConn %s cannot be reuse status: %s",
                            amNode.getServiceInstance(), amNode.getNodeStatus()));
                    return false;
                  }
                } else {
                  return NodeStatus.isAvailable(node.getNodeStatus());
                }
              })
          .toArray(Node[]::new);
    } else {
      return nodes;
    }
  }
}
