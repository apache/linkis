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

/*
 * Created by ${USER} on ${DATE}.
 */

package com.webank.wedatasphere.linkis.httpclient.request

import java.util

import com.ning.http.client.cookie.Cookie

/**
  * Created by enjoyyin on 2019/5/16.
  */
trait HttpAction extends Action {

  private val headerParams: util.Map[String, String] = new util.HashMap[String, String]
  private val cookies = new util.ArrayList[Cookie]

  def getHeaders: util.Map[String, String] = headerParams
  def addHeader(key: String, value: String): Unit = headerParams.put(key, value)

  def getCookies: Array[Cookie] = cookies.toArray(new Array[Cookie](cookies.size()))
  def addCookie(cookie: javax.servlet.http.Cookie): Unit =
    cookies.add(Cookie.newValidCookie(cookie.getName, cookie.getValue, cookie.getDomain,
      cookie.getValue, cookie.getPath, -1, cookie.getMaxAge, cookie.getSecure, true))
  def addCookie(cookie: Cookie): Unit = cookies.add(cookie)

  def getURL: String
}
