package com.webank.wedatasphere.linkis.ecm.server.hook

import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest

trait ECMHook {

  def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn): Unit

  def afterLaunch(conn: EngineConn): Unit

  def getName: String
}

object ECMHook{
  val ecmHooks = Array[ECMHook](new JarUDFLoadECMHook)
  def getECMHooks = ecmHooks
}
