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

import org.apache.linkis.manager.common.entity.metrics.NodeTaskInfo;
import org.apache.linkis.manager.common.entity.node.AMNode;
import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(4)
public class TaskInfoNodeSelectRule implements NodeSelectRule {
  private static final Logger logger = LoggerFactory.getLogger(TaskInfoNodeSelectRule.class);

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (nodes != null) {
      Arrays.sort(nodes, this::sortByTaskInfo);
    }
    return nodes;
  }

  /**
   * sort by label score
   *
   * @param nodeA
   * @param nodeB
   * @return
   */
  private int sortByTaskInfo(Node nodeA, Node nodeB) {
    if (nodeA instanceof AMNode && nodeB instanceof AMNode) {
      try {
        if (getTasks(((AMNode) nodeA).getNodeTaskInfo())
            < getTasks(((AMNode) nodeB).getNodeTaskInfo())) {
          return 1;
        } else {
          return -1;
        }
      } catch (Throwable t) {
        logger.warn("Failed to Compare resource ", t);
        return 1;
      }
    } else {
      return -1;
    }
  }

  private int getTasks(NodeTaskInfo nodeTaskInfo) {
    return nodeTaskInfo == null ? 0 : nodeTaskInfo.getTasks();
  }
}
