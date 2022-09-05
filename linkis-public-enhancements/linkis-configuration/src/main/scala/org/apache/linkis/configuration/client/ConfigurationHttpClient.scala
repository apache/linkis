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

package org.apache.linkis.configuration.client

import org.apache.linkis.configuration.request.{ConfigrationResourceAction, EmsListAction}
import org.apache.linkis.configuration.response.EmsListResult
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.httpclient.response.Result

import java.io.Closeable
import java.util.concurrent.TimeUnit

abstract class ConfigurationHttpClient extends Closeable {

  protected[client] def executeJob(ujesJobAction: ConfigrationResourceAction): Result

  def list(emsListAction: EmsListAction): EmsListResult = {
    executeJob(emsListAction).asInstanceOf[EmsListResult]
  }


}

object ConfigurationHttpClient {

  def apply(clientConfig: DWSClientConfig): ConfigurationHttpClient = new ConfigurationHttpClientImpl(
    clientConfig
  )

  def apply(serverUrl: String): ConfigurationHttpClient = apply(serverUrl, 30000, 10)

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int): ConfigurationHttpClient =
    apply(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def apply(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int,
      authenticationStrategy: AuthenticationStrategy,
      dwsVersion: String
  ): ConfigurationHttpClient = {
    val clientConfig = DWSClientConfigBuilder
      .newBuilder()
      .addServerUrl(serverUrl)
      .connectionTimeout(30000)
      .discoveryEnabled(false)
      .loadbalancerEnabled(false)
      .maxConnectionSize(maxConnection)
      .retryEnabled(false)
      .readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setDWSVersion(dwsVersion)
      .build()
    apply(clientConfig)
  }

  def getDiscoveryClient(serverUrl: String): ConfigurationHttpClient =
    getDiscoveryClient(serverUrl, 30000, 10)

  def getDiscoveryClient(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int
  ): ConfigurationHttpClient =
    getDiscoveryClient(
      serverUrl,
      readTimeout,
      maxConnection,
      new StaticAuthenticationStrategy,
      "v1"
    )

  def getDiscoveryClient(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int,
      authenticationStrategy: AuthenticationStrategy,
      dwsVersion: String
  ): ConfigurationHttpClient = {
    val clientConfig = DWSClientConfigBuilder
      .newBuilder()
      .addServerUrl(serverUrl)
      .connectionTimeout(30000)
      .discoveryEnabled(true)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(true)
      .maxConnectionSize(maxConnection)
      .retryEnabled(false)
      .readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setDWSVersion(dwsVersion)
      .build()
    apply(clientConfig)
  }

}
