package com.webank.wedatasphere.linkis.manager.am.service

import com.webank.wedatasphere.linkis.manager.am.manager.EngineNodeManager


trait EngineService {



  def getEngineNodeManager:EngineNodeManager

  def getEMService(): EMEngineService

}
