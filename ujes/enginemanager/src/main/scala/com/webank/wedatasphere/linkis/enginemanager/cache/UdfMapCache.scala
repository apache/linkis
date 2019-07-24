/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.enginemanager.cache

import java.util

import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_UDF_APP_NAME
import com.webank.wedatasphere.linkis.protocol.CacheableProtocol
import com.webank.wedatasphere.linkis.rpc.RPCMapCache
import com.webank.wedatasphere.linkis.udf.api.rpc.{RequestUdfTree, ResponseUdfTree}
import com.webank.wedatasphere.linkis.udf.entity.UDFTree

object UdfMapCache {
  val udfMapCache = new RPCMapCache[RequestUdfTree, String, UDFTree](
    ENGINE_UDF_APP_NAME.getValue)  {
    override protected def createRequest(key: RequestUdfTree): CacheableProtocol = key

    override protected def createMap(any: Any): util.Map[String, UDFTree] = any match {
      case response: ResponseUdfTree =>
        val result = new util.HashMap[String, UDFTree]()
        result.put("udfTree", response.udfTree)
        result
    }
  }
}
