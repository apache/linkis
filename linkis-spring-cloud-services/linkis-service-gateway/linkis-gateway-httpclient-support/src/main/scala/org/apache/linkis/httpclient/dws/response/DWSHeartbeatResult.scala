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

package org.apache.linkis.httpclient.dws.response

import org.apache.linkis.httpclient.discovery.HeartbeatResult

import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils

import java.util

class DWSHeartbeatResult(response: HttpResponse, serverUrl: String)
    extends HeartbeatResult
    with DWSResult {

  setResponse()

  private def setResponse(): Unit = {
    val entity = response.getEntity
    val responseBody: String = if (entity != null) {
      EntityUtils.toString(entity, "UTF-8")
    } else {
      null
    }
    val statusCode: Int = response.getStatusLine.getStatusCode
    val url: String = serverUrl
    val contentType: String = entity.getContentType.getValue
    set(responseBody, statusCode, url, contentType)
  }

  if (getStatus != 0) {
    logger.warn(s"heartbeat to gateway $serverUrl failed! message: $getMessage.")
  }

  override val isHealthy: Boolean = getData.get("isHealthy") match {
    case b: java.lang.Boolean => b
    case s if s != null => s.toString.toBoolean
    case _ => false
  }

  def getGatewayList: Array[String] = getData.get("gatewayList") match {
    case l: util.List[String] => l.toArray(new Array[String](l.size())).map("http://" + _)
    case array: Array[String] => array.map("http://" + _)
    case _ => Array.empty
  }

}
