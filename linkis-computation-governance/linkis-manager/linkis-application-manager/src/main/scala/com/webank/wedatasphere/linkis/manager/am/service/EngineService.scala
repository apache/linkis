package com.webank.wedatasphere.linkis.manager.am.service

import com.webank.wedatasphere.linkis.manager.am.manager.EngineNodeManager

/**
  * @author peacewong
  * @date 2020/6/12 15:26
  */
trait EngineService {



  def getEngineNodeManager:EngineNodeManager

  def getEMService(): EMEngineService

}
