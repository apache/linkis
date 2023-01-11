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

package org.apache.linkis.gateway.http

import org.apache.linkis.server.JMap

import org.springframework.http.server.reactive.AbstractServerHttpRequest

import javax.servlet.http.Cookie

import java.net.{InetSocketAddress, URI}

trait GatewayHttpRequest {

  def getRequestURI: String

  def getURI: URI

  def getHeaders: JMap[String, Array[String]]

  def addHeader(headerName: String, headers: Array[String]): Unit

  def getQueryParams: JMap[String, Array[String]]

  def addCookie(cookieName: String, cookies: Array[Cookie]): Unit

  def getCookies: JMap[String, Array[Cookie]]

  def getRemoteAddress: InetSocketAddress

  def getRequestRealIpAddr(): String

  def getMethod: String

  def getRequestBody: String

}
