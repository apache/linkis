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

import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(0)
public class ScoreNodeSelectRule implements NodeSelectRule {

  private static final Logger logger = LoggerFactory.getLogger(ScoreNodeSelectRule.class);

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (nodes != null) {
      Arrays.sort(nodes, this.sortByScore());
    }
    return nodes;
  }

  /**
   * sort by label score
   *
   * @return TODO
   */
  private Comparator<Node> sortByScore() {
    return (nodeA, nodeB) -> {
      if (nodeA instanceof ScoreServiceInstance && nodeB instanceof ScoreServiceInstance) {
        ScoreServiceInstance instanceA = (ScoreServiceInstance) nodeA;
        ScoreServiceInstance instanceB = (ScoreServiceInstance) nodeB;
        try {
          if (instanceA.getScore() > instanceB.getScore()) {
            return -1;
          }
        } catch (Exception e) {
          logger.warn("Failed to Compare resource ", e);
          return -1;
        }
        return 1;
      } else {
        return -1;
      }
    };
  }
}
