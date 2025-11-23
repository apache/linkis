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

package org.apache.linkis.httpclient.loadbalancer

import org.apache.linkis.httpclient.discovery.DiscoveryListener

import java.util

abstract class AbstractLoadBalancer extends LoadBalancer with DiscoveryListener {

  private val serverUrls = new util.HashSet[String]
  private val unhealthyServerUrls = new util.HashSet[String]

  override def onServerDiscovered(serverUrl: String): Unit = {
    serverUrls synchronized serverUrls.add(serverUrl)
    if (unhealthyServerUrls.contains(serverUrl)) unhealthyServerUrls synchronized {
      unhealthyServerUrls.remove(serverUrl)
    }
  }

  override def onServerUnconnected(serverUrl: String): Unit = {
    serverUrls synchronized serverUrls.remove(serverUrl)
    unhealthyServerUrls synchronized unhealthyServerUrls.remove(serverUrl)
  }

  override def onServerUnhealthy(serverUrl: String): Unit = {
    unhealthyServerUrls synchronized unhealthyServerUrls.add(serverUrl)
  }

  override def onServerHealthy(serverUrl: String): Unit = {
    unhealthyServerUrls synchronized unhealthyServerUrls.remove(serverUrl)
    serverUrls synchronized serverUrls.add(serverUrl)
  }

  override def getAllServerUrls: Array[String] =
    serverUrls.toArray(new Array[String](serverUrls.size()))

  def getAllUnhealthyServerUrls: Array[String] =
    unhealthyServerUrls.toArray(new Array[String](unhealthyServerUrls.size()))

}
