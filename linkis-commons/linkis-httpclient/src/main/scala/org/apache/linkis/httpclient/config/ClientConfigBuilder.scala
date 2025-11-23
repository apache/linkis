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

import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{DefaultRetryHandler, RetryHandler}
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.loadbalancer.LoadBalancerStrategy

import scala.concurrent.duration.TimeUnit

class ClientConfigBuilder protected () {

  protected var serverUrl: String = _
  protected var discoveryEnabled: Boolean = false
  protected var discoveryPeriod: Long = _
  protected var discoveryTimeUnit: TimeUnit = _
  protected var loadbalancerEnabled: Boolean = false
  protected var loadbalancerStrategy: LoadBalancerStrategy = _
  protected var authenticationStrategy: AuthenticationStrategy = _
  protected var authTokenKey: String = _
  protected var authTokenValue: String = _
  protected var connectTimeout: Long = _
  protected var readTimeout: Long = _
  protected var maxConnection: Int = _
  protected var retryEnabled: Boolean = true

  protected var ssl: Boolean = false

  protected var retryHandler: RetryHandler = {
    val retryHandler = new DefaultRetryHandler
    retryHandler.addRetryException(classOf[LinkisRetryException])
    retryHandler
  }

  def addServerUrl(serverUrl: String): this.type = {
    this.serverUrl = serverUrl
    this
  }

  def discoveryEnabled(isDiscoveryEnabled: Boolean): this.type = {
    this.discoveryEnabled = isDiscoveryEnabled
    this
  }

  def discoveryFrequency(period: Long, timeUnit: TimeUnit): this.type = {
    this.discoveryPeriod = period
    this.discoveryTimeUnit = timeUnit
    this
  }

  def loadbalancerEnabled(isBalanceEnabled: Boolean): this.type = {
    this.loadbalancerEnabled = isBalanceEnabled
    this
  }

  def setBalancerStrategy(loadbalancerStrategy: LoadBalancerStrategy): this.type = {
    this.loadbalancerStrategy = loadbalancerStrategy
    this
  }

  def setAuthenticationStrategy(authenticationStrategy: AuthenticationStrategy): this.type = {
    this.authenticationStrategy = authenticationStrategy
    this
  }

  def connectionTimeout(connectTimeout: Long): this.type = {
    this.connectTimeout = connectTimeout
    this
  }

  def readTimeout(readTimeout: Long): this.type = {
    this.readTimeout = readTimeout
    this
  }

  def maxConnectionSize(maxConnection: Int): this.type = {
    this.maxConnection = maxConnection
    this
  }

  def retryEnabled(isRetryEnabled: Boolean): this.type = {
    this.retryEnabled = isRetryEnabled
    this
  }

  def setRetryHandler(retryHandler: RetryHandler): this.type = {
    this.retryHandler = retryHandler
    this
  }

  def setAuthTokenKey(authTokenKey: String): this.type = {
    this.authTokenKey = authTokenKey
    this
  }

  def setAuthTokenValue(authTokenValue: String): this.type = {
    this.authTokenValue = authTokenValue
    this
  }

  def setSSL(isSSL: Boolean): this.type = {
    this.ssl = isSSL
    this
  }

  def build(): ClientConfig = new ClientConfig(
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
    authTokenValue,
    ssl
  )

}

object ClientConfigBuilder {
  def newBuilder(): ClientConfigBuilder = new ClientConfigBuilder
}
