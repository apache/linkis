/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.rpc.interceptor.common

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.protocol.CacheableProtocol
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.interceptor.{
  RPCInterceptor,
  RPCInterceptorChain,
  RPCInterceptorExchange
}

import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

import java.util.concurrent.{Callable, TimeUnit}

import com.google.common.cache.{Cache, CacheBuilder, RemovalListener, RemovalNotification}

@Component
class CacheableRPCInterceptor extends RPCInterceptor with Logging {

  private val guavaCache: Cache[Any, Any] = CacheBuilder
    .newBuilder()
    .concurrencyLevel(5)
    .expireAfterAccess(
      RPCConfiguration.BDP_RPC_CACHE_CONF_EXPIRE_TIME.getValue,
      TimeUnit.MILLISECONDS
    )
    .initialCapacity(20) // TODO Make parameters(做成参数)
    .maximumSize(1000)
    .recordStats()
    .removalListener(new RemovalListener[Any, Any] {

      override def onRemoval(removalNotification: RemovalNotification[Any, Any]): Unit = {
        logger.debug(
          s"CacheSender removed key => ${removalNotification.getKey}, value => ${removalNotification.getValue}."
        )
      }

    })
    .asInstanceOf[CacheBuilder[Any, Any]]
    .build()

  override val order: Int = 10

  override def intercept(
      interceptorExchange: RPCInterceptorExchange,
      chain: RPCInterceptorChain
  ): Any = interceptorExchange.getProtocol match {
    case cacheable: CacheableProtocol =>
      guavaCache.get(
        cacheable.toString,
        new Callable[Any] {

          override def call(): Any = {
            val returnMsg = chain.handle(interceptorExchange)
            returnMsg match {
              case warn: WarnException =>
                throw warn
              case _ =>
                returnMsg
            }
          }

        }
      )
    case _ => chain.handle(interceptorExchange)
  }

  def removeCache(cacheKey: String): Boolean = {
    Utils.tryCatch {
      if (!StringUtils.isEmpty(cacheKey)) {
        guavaCache.invalidate(cacheKey)
      }
      true
    } { case exception: Exception =>
      logger.warn(s"Failed to clean RPC cache, cache key:${cacheKey}", exception)
      false
    }
  }

}
