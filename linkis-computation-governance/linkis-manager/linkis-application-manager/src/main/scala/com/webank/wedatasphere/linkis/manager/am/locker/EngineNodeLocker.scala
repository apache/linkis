package com.webank.wedatasphere.linkis.manager.am.locker

import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode


trait EngineNodeLocker {


  def lockEngine(engineNode: EngineNode, timeout: Long): Option[String]


  def releaseLock(engineNode: EngineNode, lock: String): Unit


}
