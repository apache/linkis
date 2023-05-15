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
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** new ecm node will be added to last */
@Component
@Order(7)
public class NewECMStandbyRule implements NodeSelectRule {

  private static final Logger logger = LoggerFactory.getLogger(NewECMStandbyRule.class);

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    if (nodes != null && nodes.length > 0) {
      try {
        if (nodes[0] instanceof EMNode) {
          List<EMNode> newEcmList = new ArrayList<>();
          List<EMNode> nullStarttimeList = new ArrayList<>();
          List<EMNode> sortedList = new ArrayList<>(nodes.length);
          long nowTime = System.currentTimeMillis();
          for (Node node : nodes) {
            EMNode emNode = (EMNode) node;
            if (emNode.getStartTime() != null) {
              if (nowTime - emNode.getStartTime().getTime()
                  < (long) AMConfiguration.EM_NEW_WAIT_MILLS.getValue()) {
                logger.info(
                    String.format(
                        "EMNode : %s with createTime : %s is new, will standby.",
                        emNode.getServiceInstance().getInstance(), emNode.getStartTime()));
                newEcmList.add(emNode);
              } else {
                sortedList.add(emNode);
              }
            } else {
              nullStarttimeList.add(emNode);
            }
          }
          newEcmList.sort(Comparator.comparingLong(emNode -> emNode.getStartTime().getTime()));
          sortedList.addAll(newEcmList);
          sortedList.addAll(nullStarttimeList);
          return sortedList.toArray(new Node[0]);
        } else {
          return nodes;
        }
      } catch (Exception e) {
        logger.error("Sort Failed because : " + e.getMessage() + ", will not sort.");
        return nodes;
      }
    } else {
      return nodes;
    }
  }
}
