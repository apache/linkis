/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.ecm.cache

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineAsyncResponse, EngineCreateError}
import com.webank.wedatasphere.linkis.orchestrator.ecm.conf.ECMPluginConf
import com.webank.wedatasphere.linkis.orchestrator.ecm.exception.ECMPluginCacheException
import org.apache.commons.lang.exception.ExceptionUtils

import scala.concurrent.duration.Duration


trait EngineAsyncResponseCache {

  @throws[ECMPluginCacheException]
  def put(id: String, engineAsyncResponse: EngineAsyncResponse): Unit

  def get(id: String, timeout: Duration): EngineAsyncResponse

  def getAndRemove(id: String, timeout: Duration): EngineAsyncResponse

}


object EngineAsyncResponseCache {

  private val engineAsyncResponseCache: EngineAsyncResponseCache = new EngineAsyncResponseCacheMap

  def getCache: EngineAsyncResponseCache = engineAsyncResponseCache

}

class EngineAsyncResponseCacheMap extends EngineAsyncResponseCache {

  private val cacheMap: java.util.Map[String, EngineAsyncResponse] = new java.util.concurrent.ConcurrentHashMap[String, EngineAsyncResponse]()

  override def get(id: String, timeout: Duration): EngineAsyncResponse = {
    Utils.waitUntil(() => cacheMap.containsKey(id), timeout)
    cacheMap.get(id)
  }

  override def getAndRemove(id: String, timeout: Duration): EngineAsyncResponse = {
    try {
      Utils.waitUntil(() => cacheMap.containsKey(id), timeout)
    } catch {
      case t: Throwable =>
        put(id, EngineCreateError(id, ExceptionUtils.getRootCauseStackTrace(t).mkString("\n")))
        throw t
    }
    cacheMap.remove(id)
  }

  @throws[ECMPluginCacheException]
  override def put(id: String, engineAsyncResponse: EngineAsyncResponse): Unit = {
    if (cacheMap.containsKey(id)) {
      cacheMap.remove(id)
      throw new ECMPluginCacheException(ECMPluginConf.ECM_CACHE_ERROR_CODE, "id duplicate")
    }
    cacheMap.put(id, engineAsyncResponse)
  }

}