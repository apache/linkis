/*
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
 */

package com.webank.wedatasphere.linkis.manager.label.service

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.node.ScoreServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait NodeLabelService {

  /**
   * Attach labels to node instance
   *
   * @param instance node instance
   * @param labels   label list
   */
  def addLabelsToNode( instance: ServiceInstance, labels: util.List[Label[_]]): Unit

  def addLabelToNode(instance: ServiceInstance, label: Label[_]): Unit

  /**
   * Update
   * @param instance node instance
   * @param label label
   */
  def updateLabelToNode(instance: ServiceInstance, label: Label[_]): Unit

  def updateLabelsToNode(instance: ServiceInstance, label: util.List[Label[_]]): Unit

  /**
   * Remove the labels related by node instance
   *
   * @param instance node instance
   * @param labels   labels
   */
  def removeLabelsFromNode(instance: ServiceInstance, labels: util.List[Label[_]]): Unit

  def removeLabelsFromNode(instance: ServiceInstance): Unit


  /**
   * Get node instances by labels
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  def getNodesByLabels(labels: util.List[Label[_]]): util.List[ServiceInstance]


  def removeLabelsFromNodeWithoutPermanent(instance: ServiceInstance, permanentLabel: Array[String])

  def getNodesByLabel(label: Label[_]): util.List[ServiceInstance]


  def getNodeLabels(instance: ServiceInstance): util.List[Label[_]]


  /**
   * Get scored node instances
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  def getScoredNodesByLabels(labels: util.List[Label[_]]): util.List[ScoreServiceInstance]


  def getScoredNodeMapsByLabels(labels: util.List[Label[_]]): util.Map[ScoreServiceInstance, util.List[Label[_]]]

}
