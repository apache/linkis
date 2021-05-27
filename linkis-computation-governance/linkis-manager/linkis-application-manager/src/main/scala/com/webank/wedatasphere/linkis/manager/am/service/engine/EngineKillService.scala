package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineInfoClearRequest}

/**
  * @author peacewong
  * @date 2020/8/4 21:36
  */
trait EngineKillService {

  def killEngine(engineInfoClearRequest: EngineInfoClearRequest): Unit

  def dealEngineRelease(engineConnReleaseRequest: EngineConnReleaseRequest): Unit
}
