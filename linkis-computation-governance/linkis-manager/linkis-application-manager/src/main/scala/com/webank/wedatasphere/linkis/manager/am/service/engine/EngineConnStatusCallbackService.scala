package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM


trait EngineConnStatusCallbackService {

  def dealEngineConnStatusCallback(engineConnStatusCallbackToAM: EngineConnStatusCallbackToAM): Unit

}
