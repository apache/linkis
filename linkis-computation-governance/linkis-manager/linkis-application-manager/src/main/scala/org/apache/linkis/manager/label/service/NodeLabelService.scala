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

package org.apache.linkis.manager.label.service

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.node.{EngineNode, ScoreServiceInstance}
import org.apache.linkis.manager.label.entity.Label

import java.util

trait NodeLabelService {

  /**
   * Attach labels to node instance
   *
   * @param instance
   *   node instance
   * @param labels
   *   label list
   */
  def addLabelsToNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit

  def addLabelToNode(instance: ServiceInstance, label: Label[_]): Unit

  /**
   * Update
   * @param instance
   *   node instance
   * @param label
   *   label
   */
  def updateLabelToNode(instance: ServiceInstance, label: Label[_]): Unit

  def updateLabelsToNode(instance: ServiceInstance, label: util.List[Label[_]]): Unit

  def labelsFromInstanceToNewInstance(
      oldServiceInstance: ServiceInstance,
      newServiceInstance: ServiceInstance
  ): Unit

  /**
   * Remove the labels related by node instance
   *
   * @param instance
   *   node instance
   * @param labels
   *   labels
   */
  def removeLabelsFromNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit

  def removeLabelsFromNode(instance: ServiceInstance, isEngine: Boolean): Unit

  /**
   * Get node instances by labels
   *
   * @param labels
   *   searchableLabel or other normal labels
   * @return
   */
  def getNodesByLabels(labels: util.List[Label[_]]): util.List[ServiceInstance]

  def getNodesByLabel(label: Label[_]): util.List[ServiceInstance]

  def getNodeLabels(instance: ServiceInstance): util.List[Label[_]]

  /**
   * Get scored node instances
   *
   * @param labels
   *   searchableLabel or other normal labels
   * @return
   */
  def getScoredNodesByLabels(labels: util.List[Label[_]]): util.List[ScoreServiceInstance]

  def getScoredNodeMapsByLabels(
      labels: util.List[Label[_]]
  ): util.Map[ScoreServiceInstance, util.List[Label[_]]]

  def getScoredNodeMapsByLabelsReuse(
      labels: util.List[Label[_]],
      shuffEnable: Boolean
  ): util.Map[ScoreServiceInstance, util.List[Label[_]]]

  def getNodeLabelsByInstanceList(
      instanceList: util.List[ServiceInstance]
  ): util.HashMap[String, util.List[Label[_]]]

  def getEngineNodesWithResourceByUser(
      user: String,
      withResource: Boolean = false
  ): Array[EngineNode]

}
