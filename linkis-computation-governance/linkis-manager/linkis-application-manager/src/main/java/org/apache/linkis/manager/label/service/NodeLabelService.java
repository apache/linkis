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

package org.apache.linkis.manager.label.service;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.label.entity.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NodeLabelService {

  /**
   * Attach labels to node instance
   *
   * @param instance node instance
   * @param labels label list
   */
  void addLabelsToNode(ServiceInstance instance, List<Label<?>> labels);

  void addLabelToNode(ServiceInstance instance, Label<?> label);

  /**
   * Update
   *
   * @param instance node instance
   * @param label label
   */
  void updateLabelToNode(ServiceInstance instance, Label<?> label);

  void updateLabelsToNode(ServiceInstance instance, List<Label<?>> labels);

  /**
   * Remove the labels related by node instance
   *
   * @param instance node instance
   * @param labels labels
   */
  void removeLabelsFromNode(ServiceInstance instance, List<Label<?>> labels);

  void removeLabelsFromNode(ServiceInstance instance, boolean isEngine);

  /**
   * Get node instances by labels
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  List<ServiceInstance> getNodesByLabels(List<Label<?>> labels);

  List<ServiceInstance> getNodesByLabel(Label<?> label);

  List<Label<?>> getNodeLabels(ServiceInstance instance);

  /**
   * Get scored node instances
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  List<ScoreServiceInstance> getScoredNodesByLabels(List<Label<?>> labels);

  Map<ScoreServiceInstance, List<Label<?>>> getScoredNodeMapsByLabels(List<Label<?>> labels);

  HashMap<String, List<Label<?>>> getNodeLabelsByInstanceList(List<ServiceInstance> instanceList);
}
