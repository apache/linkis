package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM

/**
  * @author peacewong
  * @date 2021/1/1 20:56
  */
trait EngineConnStatusCallbackService {

  def dealEngineConnStatusCallback(engineConnStatusCallbackToAM: EngineConnStatusCallbackToAM): Unit

}
