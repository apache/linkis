package com.webank.wedatasphere.linkis.manager.am.manager

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode, Node, ScoreServiceInstance}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineStopRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest


trait EMNodeManager {

  def emRegister(emNode: EMNode): Unit

  def listEngines(emNode: EMNode): java.util.List[EngineNode]

  def listUserEngines(emNode: EMNode, user: String): java.util.List[EngineNode]

  def listUserNodes(user: String): java.util.List[Node]

  /**
    * Get detailed em information from the persistence
    *
    * @param scoreServiceInstances
    * @return
    */
  def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode]

  def getEM(serviceInstance: ServiceInstance): EMNode

  def stopEM(emNode: EMNode): Unit

  def deleteEM(emNode: EMNode): Unit

  def pauseEM(serviceInstance: ServiceInstance): Unit

  /**
    * 1. request engineManager to launch engine
    * 2. persist engine info
    *
    * @param engineBuildRequest
    * @param emNode
    * @return
    */
  def createEngine(engineBuildRequest: EngineConnBuildRequest, emNode: EMNode): EngineNode

  def stopEngine(engineStopRequest: EngineStopRequest, emNode: EMNode): Unit


  def addEMNodeInstance(emNode: EMNode): Unit

  def initEMNodeMetrics(emNode: EMNode): Unit
}
