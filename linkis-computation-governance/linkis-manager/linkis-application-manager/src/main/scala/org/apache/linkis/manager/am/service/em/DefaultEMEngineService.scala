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

package org.apache.linkis.manager.am.service.em

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.server.service.EngineConnLaunchService
import org.apache.linkis.manager.am.exception.AMErrorException
import org.apache.linkis.manager.am.manager.{EMNodeManager, EngineNodeManager}
import org.apache.linkis.manager.am.service.EMEngineService
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.node._
import org.apache.linkis.manager.common.protocol.em._
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.label.entity.{EngineNodeLabel, Label}
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.service.common.label.LabelFilter

import org.apache.commons.collections.MapUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._

@Service
class DefaultEMEngineService extends EMEngineService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var engineNodeManager: EngineNodeManager = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var engineConnLaunchService: EngineConnLaunchService = _

  @Autowired
  private var labelFilter: LabelFilter = _

  override def listEngines(getEMEnginesRequest: GetEMEnginesRequest): util.List[EngineNode] = {
    val emNode = new AMEMNode()
    emNode.setServiceInstance(getEMEnginesRequest.getEm)
    emNodeManager.listEngines(emNode)
  }

  override def createEngine(
      engineBuildRequest: EngineConnBuildRequest,
      emNode: EMNode
  ): EngineNode = {

    logger.info(s"EM ${emNode.getServiceInstance} start to create Engine ${engineBuildRequest}")
    val engineConnLaunchRequest =
      engineConnLaunchService.createEngineConnLaunchRequest(engineBuildRequest)

    val engineNode = emNodeManager.createEngine(engineConnLaunchRequest, emNode)
    logger.info(
      s"EM ${emNode.getServiceInstance} Finished to create Engine ${engineBuildRequest.ticketId}"
    )
    engineNode.setLabels(emNode.getLabels.asScala.filter(_.isInstanceOf[EngineNodeLabel]).asJava)
    engineNode.setEMNode(emNode)
    engineNode

  }

  override def stopEngine(engineNode: EngineNode, emNode: EMNode): Unit = {
    if (null == emNode) {
      logger.error(s" The emNode of ${engineNode.getServiceInstance} is null")
      return
    }
    logger.debug(
      s"EM ${emNode.getServiceInstance} start to stop Engine ${engineNode.getServiceInstance}"
    )
    val engineStopRequest = new EngineStopRequest
    engineStopRequest.setServiceInstance(engineNode.getServiceInstance)
    emNodeManager.stopEngine(engineStopRequest, emNode)
    // engineNodeManager.deleteEngineNode(engineNode)
    logger.info(
      s"EM ${emNode.getServiceInstance} finished to stop Engine ${engineNode.getServiceInstance}"
    )
  }

  override def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode] = {
    emNodeManager.getEMNodes(scoreServiceInstances)
  }

  override def getEMNodes(labels: util.List[Label[_]]): Array[EMNode] = {
    val instanceAndLabels =
      nodeLabelService.getScoredNodeMapsByLabels(labelFilter.choseEMLabel(labels))
    if (MapUtils.isEmpty(instanceAndLabels)) {
      new AMErrorException(AMConstant.EM_ERROR_CODE, "No corresponding EM")
    }
    // TODO add em select rule to do this
    val emInstanceLabelOption = labels.asScala.find(_.isInstanceOf[EMInstanceLabel])
    val filterInstanceAndLabel = if (emInstanceLabelOption.isDefined) {
      val emInstanceLabel = emInstanceLabelOption.get.asInstanceOf[EMInstanceLabel]
      logger.info(s"use emInstanceLabel , will be route to ${emInstanceLabel.getServiceInstance}")
      if (!instanceAndLabels.asScala.exists(_._1.equals(emInstanceLabel.getServiceInstance))) {
        throw new AMErrorException(
          AMConstant.EM_ERROR_CODE,
          s"You specified em ${emInstanceLabel.getServiceInstance}, but the corresponding EM does not exist in the Manager"
        )
      }
      instanceAndLabels.asScala.filter(
        _._1.getServiceInstance.equals(emInstanceLabel.getServiceInstance)
      )
    } else {
      instanceAndLabels.asScala.toMap
    }
    val nodes = getEMNodes(filterInstanceAndLabel.keys.toArray)
    if (null == nodes) {
      return null
    }
    nodes.foreach { node =>
      val persistenceLabel = filterInstanceAndLabel
        .find(_._1.getServiceInstance.equals(node.getServiceInstance))
        .map(_._2)
      persistenceLabel.foreach(labelList =>
        node.setLabels(labelList.asScala.map(ManagerUtils.persistenceLabelToRealLabel).asJava)
      )
    }
    nodes
  }

}
