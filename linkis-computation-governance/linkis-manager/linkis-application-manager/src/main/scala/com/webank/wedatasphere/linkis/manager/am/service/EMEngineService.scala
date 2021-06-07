package com.webank.wedatasphere.linkis.manager.am.service

import java.util

import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode, ScoreServiceInstance}
import com.webank.wedatasphere.linkis.manager.common.protocol.em._
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait EMEngineService {


  def listEngines(getEMEnginesRequest: GetEMEnginesRequest): util.List[EngineNode]

  def createEngine(engineBuildRequest: EngineConnBuildRequest, emNode: EMNode): EngineNode

  def stopEngine(engineNode: EngineNode, EMNode: EMNode): Unit

  def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode]

  def getEMNodes(labels: util.List[Label[_]]): Array[EMNode]

}
