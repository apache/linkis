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

package org.apache.linkis.httpclient.dws.config

import org.apache.linkis.common.utils.RetryHandler
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.config.ClientConfig
import org.apache.linkis.httpclient.loadbalancer.LoadBalancerStrategy

import scala.concurrent.duration.TimeUnit

class DWSClientConfig private[config] (
    serverUrl: String,
    discoveryEnabled: Boolean,
    discoveryPeriod: Long,
    discoveryTimeUnit: TimeUnit,
    loadbalancerEnabled: Boolean,
    loadbalancerStrategy: LoadBalancerStrategy,
    authenticationStrategy: AuthenticationStrategy,
    connectTimeout: Long,
    readTimeout: Long,
    maxConnection: Int,
    retryEnabled: Boolean,
    retryHandler: RetryHandler,
    authTokenKey: String,
    authTokenValue: String
) extends ClientConfig(
      serverUrl,
      discoveryEnabled,
      discoveryPeriod,
      discoveryTimeUnit,
      loadbalancerEnabled,
      loadbalancerStrategy,
      authenticationStrategy,
      connectTimeout,
      readTimeout,
      maxConnection,
      retryEnabled,
      retryHandler,
      authTokenKey,
      authTokenValue
    ) {

  def this(clientConfig: ClientConfig) = this(
    clientConfig.getServerUrl,
    clientConfig.isDiscoveryEnabled,
    clientConfig.getDiscoveryPeriod,
    clientConfig.getDiscoveryTimeUnit,
    clientConfig.isLoadbalancerEnabled,
    clientConfig.getLoadbalancerStrategy,
    clientConfig.getAuthenticationStrategy,
    clientConfig.getConnectTimeout,
    clientConfig.getReadTimeout,
    clientConfig.getMaxConnection,
    clientConfig.isRetryEnabled,
    clientConfig.getRetryHandler,
    clientConfig.getAuthTokenKey,
    clientConfig.getAuthTokenValue
  )

  private var dwsVersion: String = _

  def setDWSVersion(dwsVersion: String): Unit = this.dwsVersion = dwsVersion
  def getDWSVersion: String = dwsVersion

}
