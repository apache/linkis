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

package org.apache.linkis.httpclient.dws.discovery

import org.apache.linkis.httpclient.config.HttpClientConstant
import org.apache.linkis.httpclient.discovery.{AbstractDiscovery, HeartbeatAction, HeartbeatResult}
import org.apache.linkis.httpclient.dws.request.DWSHeartbeatAction
import org.apache.linkis.httpclient.dws.response.DWSHeartbeatResult

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse

import java.util.concurrent.ScheduledFuture

class DefaultConfigDiscovery extends AbstractDiscovery {

  override protected def discovery(): Array[String] = {
    if (StringUtils.isNotBlank(getServerUrl)) {
      getServerUrl.split(HttpClientConstant.URL_SPLIT_TOKEN).filter(StringUtils.isNotBlank)
    } else {
      Array.empty[String]
    }
  }

  override def startDiscovery(): ScheduledFuture[_] = {
    val serverUrls = discovery()
    addServerInstances(serverUrls)
    serverUrls.foreach(url => logger.info(s"discovery config url $url"))
    null
  }

  override protected def getHeartbeatAction(serverUrl: String): HeartbeatAction =
    new DWSHeartbeatAction(serverUrl)

  override def getHeartbeatResult(
      response: HttpResponse,
      requestAction: HeartbeatAction
  ): HeartbeatResult = requestAction match {
    case h: DWSHeartbeatAction => new DWSHeartbeatResult(response, h.serverUrl)
  }

}
