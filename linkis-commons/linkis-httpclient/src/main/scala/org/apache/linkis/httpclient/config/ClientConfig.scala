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

package org.apache.linkis.httpclient.config

import org.apache.linkis.common.utils.RetryHandler
import org.apache.linkis.httpclient.authentication.{
  AbstractAuthenticationStrategy,
  AuthenticationStrategy
}
import org.apache.linkis.httpclient.loadbalancer.LoadBalancerStrategy

import org.apache.commons.lang3.StringUtils

import scala.concurrent.duration.TimeUnit

class ClientConfig private () {

  private var serverUrl: String = _
  private var discoveryEnabled: Boolean = false
  private var discoveryPeriod: Long = _
  private var discoveryTimeUnit: TimeUnit = _
  private var loadbalancerEnabled: Boolean = false
  private var loadbalancerStrategy: LoadBalancerStrategy = _
  private var authenticationStrategy: AuthenticationStrategy = _
  private var authTokenKey: String = _
  private var authTokenValue: String = _
  private var connectTimeout: Long = _
  private var readTimeout: Long = _
  private var maxConnection: Int = 20
  private var retryEnabled: Boolean = _
  private var retryHandler: RetryHandler = _
  private var ssl: Boolean = false

  protected[config] def this(
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
      authTokenValue: String,
      isSSL: Boolean = false
  ) = {
    this()
    this.serverUrl = serverUrl

    this.discoveryEnabled = discoveryEnabled

    this.discoveryPeriod = discoveryPeriod
    this.discoveryTimeUnit = discoveryTimeUnit
    this.loadbalancerEnabled = loadbalancerEnabled
    this.loadbalancerStrategy = loadbalancerStrategy
    this.authenticationStrategy = authenticationStrategy
    this.connectTimeout = connectTimeout
    this.readTimeout = readTimeout
    this.maxConnection = maxConnection
    this.retryEnabled = retryEnabled
    this.retryHandler = retryHandler
    this.authTokenKey = authTokenKey
    this.authTokenValue = authTokenValue
    this.ssl = isSSL
    authenticationStrategy match {
      case ab: AbstractAuthenticationStrategy => ab.setClientConfig(this)
      case _ =>
    }
  }

  def getServerUrl: String = serverUrl

  def getDefaultServerUrl: String = {
    if (
        StringUtils
          .isNotBlank(serverUrl) && serverUrl.contains(HttpClientConstant.URL_SPLIT_TOKEN)
    ) {
      serverUrl.split(HttpClientConstant.URL_SPLIT_TOKEN)(0)
    } else {
      serverUrl
    }
  }

  def isDiscoveryEnabled: Boolean = discoveryEnabled

  def getDiscoveryPeriod: Long = discoveryPeriod

  def getDiscoveryTimeUnit: TimeUnit = discoveryTimeUnit

  def isLoadbalancerEnabled: Boolean = loadbalancerEnabled

  def getLoadbalancerStrategy: LoadBalancerStrategy = loadbalancerStrategy

  def getAuthenticationStrategy: AuthenticationStrategy = authenticationStrategy

  def getAuthTokenKey: String = authTokenKey

  def getAuthTokenValue: String = authTokenValue

  def getConnectTimeout: Long = connectTimeout

  def getReadTimeout: Long = readTimeout

  def getMaxConnection: Int = maxConnection

  def isRetryEnabled: Boolean = retryEnabled

  def getRetryHandler: RetryHandler = retryHandler

  def isSSL: Boolean = ssl

}
