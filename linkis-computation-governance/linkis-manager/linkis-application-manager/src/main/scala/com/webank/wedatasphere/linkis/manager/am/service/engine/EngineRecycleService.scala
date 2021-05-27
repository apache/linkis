package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineRecyclingRequest

/**
  * @author peacewong
  * @date 2020/6/12 16:07
  */
trait EngineRecycleService {

  @throws[LinkisRetryException]
  def recycleEngine(engineRecyclingRequest: EngineRecyclingRequest): Array[ServiceInstance]

}
