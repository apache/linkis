/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.service.em

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorException
import com.webank.wedatasphere.linkis.manager.am.manager.{EMNodeManager, EngineNodeManager}
import com.webank.wedatasphere.linkis.manager.am.service.EMEngineService
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.node._
import com.webank.wedatasphere.linkis.manager.common.protocol.em._
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineStopRequest
import com.webank.wedatasphere.linkis.manager.common.utils.ManagerUtils
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.label.entity.{EngineNodeLabel, Label}
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.service.common.label.LabelFilter
import org.apache.commons.collections.MapUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._



@Service
class DefaultEMEngineService extends EMEngineService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var engineNodeManager: EngineNodeManager = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var labelFilter: LabelFilter = _

  override def listEngines(getEMEnginesRequest: GetEMEnginesRequest): util.List[EngineNode] = {
    val emNode = new AMEMNode()
    emNode.setServiceInstance(getEMEnginesRequest.getEm)
    emNodeManager.listEngines(emNode)
  }


  override def createEngine(engineBuildRequest: EngineConnBuildRequest, emNode: EMNode): EngineNode = {

    info(s"EM ${emNode.getServiceInstance} start to create Engine ${engineBuildRequest}")
    val engineNode = emNodeManager.createEngine(engineBuildRequest, emNode)
    info(s"EM ${emNode.getServiceInstance} Finished to create Engine ${engineBuildRequest.ticketId}")
    engineNode.setLabels(emNode.getLabels.filter(_.isInstanceOf[EngineNodeLabel]))
    engineNode.setEMNode(emNode)
    engineNode

  }

  override def stopEngine(engineNode: EngineNode, emNode: EMNode): Unit = {
    info(s"EM ${emNode.getServiceInstance} start to stop Engine ${engineNode.getServiceInstance}")
    val engineStopRequest = new EngineStopRequest
    engineStopRequest.setServiceInstance(engineNode.getServiceInstance)
    emNodeManager.stopEngine(engineStopRequest, emNode)
    //engineNodeManager.deleteEngineNode(engineNode)
    info(s"EM ${emNode.getServiceInstance} finished to stop Engine ${engineNode.getServiceInstance}")
  }

  override def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode] = {
    emNodeManager.getEMNodes(scoreServiceInstances)
  }

  override def getEMNodes(labels: util.List[Label[_]]): Array[EMNode] = {
    val instanceAndLabels = nodeLabelService.getScoredNodeMapsByLabels(labelFilter.choseEMLabel(labels))
    if (MapUtils.isEmpty(instanceAndLabels)) {
      new AMErrorException(AMConstant.EM_ERROR_CODE, "No corresponding EM")
    }
    val nodes = getEMNodes(instanceAndLabels.keys.toArray)
    if (null == nodes) {
      return null
    }
    nodes.foreach { node =>
      val persistenceLabel = instanceAndLabels.find(_._1.getServiceInstance.equals(node.getServiceInstance)).map(_._2)
      persistenceLabel.foreach(labelList => node.setLabels(labelList.map(ManagerUtils.persistenceLabelToRealLabel)))
    }
    nodes
  }

}
