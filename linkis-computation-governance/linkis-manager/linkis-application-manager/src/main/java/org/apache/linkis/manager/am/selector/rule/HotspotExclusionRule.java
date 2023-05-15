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

import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

/** Ten hotspot exclusion rules, random ordering of the previous elements */
@Component
@Order(Integer.MAX_VALUE)
public class HotspotExclusionRule implements NodeSelectRule {

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (AMConfiguration.NODE_SELECT_HOTSPOT_EXCLUSION_RULE) {
      randomShuffle(nodes);
    }
    return nodes;
  }

  /**
   * Random sorting if there are more than 10 sorting rules, the first 5 will be randomly sorted if
   * there are no more than 10, the first half will be sorted
   *
   * @param nodes
   * @return
   */
  private Node[] randomShuffle(Node[] nodes) {
    if (null == nodes || nodes.length < 3) {
      return nodes;
    }
    int shuffleSize = nodes.length <= 10 ? nodes.length / 2 : 5;
    Random random = new Random();
    for (int i = 0; i < shuffleSize; i++) {
      int nextIndex = random.nextInt(shuffleSize);
      Node tmpValue = nodes[i];
      nodes[i] = nodes[nextIndex];
      nodes[nextIndex] = tmpValue;
    }
    return nodes;
  }
}
