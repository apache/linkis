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

import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.node.RMNode;
import org.apache.linkis.manager.common.utils.ResourceUtils;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(5)
public class ResourceNodeSelectRule implements NodeSelectRule {

  private static final Logger logger = LoggerFactory.getLogger(ResourceNodeSelectRule.class);

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (nodes != null) {
      Arrays.sort(nodes, sortByResource().thenComparing(sortByResourceRate()));
    }
    return nodes;
  }

  private Comparator<Node> sortByResource() {
    return (nodeA, nodeB) -> {
      if (nodeA instanceof RMNode && nodeB instanceof RMNode) {
        try {
          RMNode nodeARm = (RMNode) nodeA;
          RMNode nodeBRm = (RMNode) nodeB;
          if (nodeARm.getNodeResource() == null
              || nodeARm.getNodeResource().getLeftResource() == null) {
            return -1;
          } else if (nodeBRm.getNodeResource() == null
              || nodeBRm.getNodeResource().getLeftResource() == null) {
            return 1;
          } else {
            if (nodeARm
                .getNodeResource()
                .getLeftResource()
                .equalsTo(nodeBRm.getNodeResource().getLeftResource())) {
              return 0;
            } else if (nodeARm
                .getNodeResource()
                .getLeftResource()
                .moreThan(nodeBRm.getNodeResource().getLeftResource())) {
              return 1;
            } else {
              return -1;
            }
          }
        } catch (Throwable t) {
          logger.warn("Failed to Compare resource " + t.getMessage());
          return 1;
        }
      } else {
        return -1;
      }
    };
  }

  private Comparator<Node> sortByResourceRate() {
    return (nodeA, nodeB) -> {
      if (!(nodeA instanceof EMNode)) {
        return 1;
      }
      if (nodeA instanceof RMNode && nodeB instanceof RMNode) {
        try {
          RMNode nodeARm = (RMNode) nodeA;
          RMNode nodeBRm = (RMNode) nodeB;
          if (nodeARm.getNodeResource() == null
              || nodeARm.getNodeResource().getLeftResource() == null) {
            return -1;
          } else if (nodeBRm.getNodeResource() == null
              || nodeBRm.getNodeResource().getLeftResource() == null) {
            return 1;
          } else {
            float aRate =
                ResourceUtils.getLoadInstanceResourceRate(
                    nodeARm.getNodeResource().getLeftResource(),
                    nodeARm.getNodeResource().getMaxResource());
            float bRate =
                ResourceUtils.getLoadInstanceResourceRate(
                    nodeBRm.getNodeResource().getLeftResource(),
                    nodeBRm.getNodeResource().getMaxResource());
            return Float.compare(aRate, bRate);
          }
        } catch (Throwable t) {
          logger.warn("Failed to Compare resource " + t.getMessage());
          return 1;
        }
      } else {
        return -1;
      }
    };
  }
}
