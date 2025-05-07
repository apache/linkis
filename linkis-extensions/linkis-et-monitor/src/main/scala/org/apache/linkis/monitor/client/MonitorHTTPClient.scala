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

package org.apache.linkis.monitor.client

import org.apache.linkis.datasource.client.response.{
  GetConnectParamsByDataSourceNameResult,
  GetInfoByDataSourceNameResult,
  GetInfoPublishedByDataSourceNameResult
}
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.httpclient.response.Result
import org.apache.linkis.monitor.request.{
  AnalyzeJobAction,
  DataSourceParamsAction,
  EmsListAction,
  EntranceTaskAction,
  KeyvalueAction,
  KillJobAction,
  MonitorAction
}
import org.apache.linkis.monitor.response.{
  AnalyzeJobResultAction,
  EntranceTaskResult,
  KeyvalueResult,
  KillJobResultAction
}
import org.apache.linkis.ujes.client.response.EmsListResult

import java.io.Closeable
import java.util.concurrent.TimeUnit

abstract class MonitorHTTPClient extends Closeable {

  protected[client] def executeJob(ujesJobAction: MonitorAction): Result

  def list(emsListAction: EmsListAction): EmsListResult = {
    executeJob(emsListAction).asInstanceOf[EmsListResult]
  }

  def entranList(entranceTaskAction: EntranceTaskAction): EntranceTaskResult = {
    executeJob(entranceTaskAction).asInstanceOf[EntranceTaskResult]
  }

  def getConfKeyValue(keyvalueAction: KeyvalueAction): KeyvalueResult = {
    executeJob(keyvalueAction).asInstanceOf[KeyvalueResult]
  }

  def getInfoByDataSourceInfo(
      datasourceInfoAction: DataSourceParamsAction
  ): GetInfoPublishedByDataSourceNameResult = {
    executeJob(datasourceInfoAction).asInstanceOf[GetInfoPublishedByDataSourceNameResult]
  }

  def killJob(killJobAction: KillJobAction): KillJobResultAction = {
    executeJob(killJobAction).asInstanceOf[KillJobResultAction]
  }

  def analyzeJob(analyzeJobAction: AnalyzeJobAction): AnalyzeJobResultAction = {
    executeJob(analyzeJobAction).asInstanceOf[AnalyzeJobResultAction]
  }

}

object MonitorHTTPClient {

  def apply(clientConfig: DWSClientConfig): MonitorHTTPClient = new MonitorHTTPClientClientImpl(
    clientConfig
  )

  def apply(serverUrl: String): MonitorHTTPClient = apply(serverUrl, 30000, 10)

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int): MonitorHTTPClient =
    apply(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def apply(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int,
      authenticationStrategy: AuthenticationStrategy,
      dwsVersion: String
  ): MonitorHTTPClient = {
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

  def getDiscoveryClient(serverUrl: String): MonitorHTTPClient =
    getDiscoveryClient(serverUrl, 30000, 10)

  def getDiscoveryClient(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int
  ): MonitorHTTPClient =
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
  ): MonitorHTTPClient = {
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
