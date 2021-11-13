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
 
package org.apache.linkis.gateway.springcloud.http

import java.net.{InetSocketAddress, URI}

import org.apache.linkis.gateway.http.GatewayHttpRequest
import org.apache.linkis.server._
import javax.servlet.http.Cookie
import org.apache.commons.lang.StringUtils
import org.springframework.http.server.reactive.AbstractServerHttpRequest

import scala.collection.JavaConversions

class SpringCloudGatewayHttpRequest(request: AbstractServerHttpRequest) extends GatewayHttpRequest {

  private val headers = {
    val headerEntrys = request.getHeaders
    val header = new JMap[String, Array[String]]
    headerEntrys.foreach{case (key, value) => if(value != null && value.nonEmpty) header.put(key, value.toArray(new Array[String](value.size())))
      else header.put(key, Array.empty)
    }
    header
  }

  private val queryParams = {
    val querys = request.getQueryParams
    val queryParams = new JMap[String, Array[String]]
    querys.foreach {case (key, value) => if(value != null && value.nonEmpty) queryParams.put(key, value.toArray(new Array[String](value.size())))
      else queryParams.put(key, Array.empty)
    }
    queryParams
  }

  private val cookies = {
    val cookieMap = request.getCookies
    val cookies = new JMap[String, Array[Cookie]]
    cookieMap.foreach {case (key, value) => if(value != null && value.nonEmpty) cookies.put(key, value.map(c => new Cookie(c.getName, c.getValue)).toArray)
    else cookies.put(key, Array.empty)}
    cookies
  }

  private val addCookies = new JMap[String, Array[Cookie]]

  private var requestBody: String = _
  private var requestURI: String = _
  private var requestAutowired = false

  def setRequestURI(requestURI: String): Unit = this.requestURI = requestURI

  def getRequest = request

  override def getRequestURI: String = if(StringUtils.isNotBlank(requestURI)) requestURI else request.getPath.pathWithinApplication.value

  override def getURI: URI = if(StringUtils.isNotBlank(requestURI)) new URI(requestURI) else request.getURI

  override def getHeaders: JMap[String, Array[String]] = headers

  override def addHeader(headerName: String, headers: Array[String]): Unit =
    request.getHeaders.addAll(headerName, JavaConversions.seqAsJavaList(headers.toList))

  override def addCookie(cookieName: String, cookies: Array[Cookie]): Unit = {
    this.cookies.put(cookieName, cookies)
    addCookies.put(cookieName, cookies)
  }

  def getAddCookies: JMap[String, Array[Cookie]] = addCookies
//  override def addCookie(cookieName: String, cookies: Array[Cookie]): Unit = request.getNativeRequest[Any] match {
//    case httpInfos: HttpInfos =>
//      httpInfos.cookies().put(cookieName, JavaConversions.setAsJavaSet(cookies.map { c =>
//        val cookie = new DefaultCookie(c.getName, c.getValue)
//        cookie.setDomain(c.getDomain)
//        cookie.setMaxAge(c.getMaxAge)
//        cookie.setPath(c.getPath)
//        cookie.setSecure(c.getSecure)
//        cookie
//      }.toSet))
//    case _ => throw new GatewayErrorException(10040, "Not support method: addCookie in GatewayHttpRequest.")
//  }

  override def getQueryParams: JMap[String, Array[String]] = queryParams

  override def getCookies: JMap[String, Array[Cookie]] = cookies

  override def getRemoteAddress: InetSocketAddress = request.getRemoteAddress

  override def getMethod: String = request.getMethodValue

  def setRequestBody(requestBody: String): Unit = {
    this.requestBody = requestBody
    requestAutowired = true
  }
  override def getRequestBody: String = requestBody

  def isRequestBodyAutowired: Boolean = requestAutowired
}
