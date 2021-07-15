package com.webank.wedatasphere.linkis.manager.am.service.cache

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, RequestQueryGlobalConfig}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.rpc.interceptor.common.CacheableRPCInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DefaultConfCacheService extends Logging {

  @Autowired
  private var cacheableRPCInterceptor: CacheableRPCInterceptor = _

  @Receiver
  def removeCacheConfiguration(removeCacheConfRequest: RemoveCacheConfRequest): Boolean = {
    if (removeCacheConfRequest.getUserCreatorLabel != null) {
      if (removeCacheConfRequest.getEngineTypeLabel != null) {
        val request = RequestQueryEngineConfig(removeCacheConfRequest.getUserCreatorLabel, removeCacheConfRequest.getEngineTypeLabel)
        cacheableRPCInterceptor.removeCache(request.toString)
        info(s"success to clear cache about configuration of ${removeCacheConfRequest.getEngineTypeLabel.getStringValue}-${removeCacheConfRequest.getEngineTypeLabel.getStringValue}")
      } else {
        val request = RequestQueryGlobalConfig(removeCacheConfRequest.getUserCreatorLabel.getUser)
        cacheableRPCInterceptor.removeCache(request.toString)
        info(s"success to clear cache about global configuration of ${removeCacheConfRequest.getUserCreatorLabel.getUser}")
      }
    }
    true
  }
}
