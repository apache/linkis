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

package org.apache.linkis.gateway.springcloud.http

import org.apache.linkis.gateway.config.GatewayConfiguration.{THIS_GATEWAY_SCHEMA, THIS_GATEWAY_URL}
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange

class LinkisGatewayHttpHeadersFilter extends HttpHeadersFilter with Ordered {

  private val GATEWAY_URL = "GATEWAY_URL"

  override def filter(input: HttpHeaders, exchange: ServerWebExchange): HttpHeaders = {
    val request: ServerHttpRequest = exchange.getRequest
    val updated: HttpHeaders = new HttpHeaders
    val iterator = input.entrySet().iterator()
    while (iterator.hasNext) {
      val next = iterator.next()
      updated.addAll(next.getKey, next.getValue)
    }
    val gatewWayURL = if (StringUtils.isNotBlank(THIS_GATEWAY_URL.getValue)) {
      THIS_GATEWAY_URL.getValue
    } else {
      val schema = if (StringUtils.isNotBlank(THIS_GATEWAY_SCHEMA.getValue)) {
        THIS_GATEWAY_SCHEMA.getValue
      } else {
        request.getURI.getScheme
      }
      s"$schema://${Sender.getThisInstance}"
    }
    updated.add(GATEWAY_URL, gatewWayURL)
    updated
  }

  override def getOrder: Int = 1
}
