package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineInfoClearRequest}


trait EngineKillService {

  def killEngine(engineInfoClearRequest: EngineInfoClearRequest): Unit

  def dealEngineRelease(engineConnReleaseRequest: EngineConnReleaseRequest): Unit
}
