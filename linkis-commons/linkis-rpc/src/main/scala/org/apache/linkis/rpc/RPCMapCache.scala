/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.rpc

import java.util

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.protocol.CacheableProtocol
import org.apache.linkis.rpc.errorcode.RPCErrorConstants
import org.apache.linkis.server.exception.FetchMapCacheFailedException


abstract class RPCMapCache[M, K, V](applicationName: String) {

  protected def createRequest(key: M): CacheableProtocol

  protected def createMap(any: Any): java.util.Map[K, V]
  private val sender = Sender.getSender(applicationName)

  def getCacheMap(key: M): util.Map[K, V] = {
    val result = Utils.tryThrow(sender.ask(createRequest(key))) {
      case error: ErrorException => error
      case t: Throwable =>
        new FetchMapCacheFailedException(RPCErrorConstants.FETCH_MAPCACHE_ERROR, "Failed to get " +
          "user " +
          "parameters! Reason: RPC request(获取用户参数失败！原因：RPC请求)" + applicationName + "Service failed!(服务失败！)", t)
    }
    createMap(result)
  }
}
