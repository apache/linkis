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
 
package org.apache.linkis.httpclient.dws.discovery

import org.apache.linkis.httpclient.discovery.{AbstractDiscovery, HeartbeatAction, HeartbeatResult}
import org.apache.linkis.httpclient.dws.request.DWSHeartbeatAction
import org.apache.linkis.httpclient.dws.response.DWSHeartbeatResult
import org.apache.http.HttpResponse

import scala.util.Random

class DWSGatewayDiscovery extends AbstractDiscovery {

  override protected def discovery(): Array[String] = {
    val serverUrl = if(getServerInstances.isEmpty) getServerUrl
      else Random.shuffle(getServerInstances.toList).head
    info("discovery to gateway " + serverUrl)
    getClient.execute(getHeartbeatAction(serverUrl)) match {
      case dws: DWSHeartbeatResult => dws.getGatewayList
    }
  }

  override protected def getHeartbeatAction(serverUrl: String): HeartbeatAction = new DWSHeartbeatAction(serverUrl)

  override def getHeartbeatResult(response: HttpResponse, requestAction: HeartbeatAction): HeartbeatResult = requestAction match {
    case h: DWSHeartbeatAction => new DWSHeartbeatResult(response, h.serverUrl)
  }
}
