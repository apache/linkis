package com.webank.wedatasphere.linkis.manager.am.manager

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EngineNode, ScoreServiceInstance}


trait EngineNodeManager {


  def listEngines(user: String): java.util.List[EngineNode]

  def getEngineNode(serviceInstance: ServiceInstance): EngineNode

  def getEngineNodeInfo(engineNode: EngineNode): EngineNode

  def getEngineNodeInfoByDB(engineNode: EngineNode): EngineNode

  /**
    * Get detailed engine information from the persistence
    *
    * @param scoreServiceInstances
    * @return
    */
  def getEngineNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EngineNode]

  def updateEngineStatus(serviceInstance: ServiceInstance, fromState: NodeStatus, toState: NodeStatus): Unit

  /**
    * add info to persistence
    *
    * @param engineNode
    */
  def addEngineNode(engineNode: EngineNode): Unit

  def updateEngineNode(serviceInstance: ServiceInstance, engineNode: EngineNode): Unit

  def updateEngine(engineNode: EngineNode): Unit

  /**
    * delete info to persistence
    *
    * @param engineNode
    */
  def deleteEngineNode(engineNode: EngineNode): Unit

  def switchEngine(engineNode: EngineNode): EngineNode

  def reuseEngine(engineNode: EngineNode): EngineNode

  def useEngine(engineNode: EngineNode): EngineNode

  def useEngine(engineNode: EngineNode, timeout: Long): EngineNode

}
