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

package org.apache.linkis.computation.client

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{RetryHandler, Utils}
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.ujes.client.{UJESClient, UJESClientImpl}
import org.apache.linkis.ujes.client.exception.{UJESClientBuilderException, UJESJobException}
import org.apache.linkis.ujes.client.request.JobSubmitAction

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

trait LinkisJobBuilder[Job <: LinkisJob] {

  protected var executeUser: String = _
  protected var jobContent: util.Map[String, AnyRef] = _
  protected var labels: util.Map[String, AnyRef] = _
  protected var params: util.Map[String, AnyRef] = _
  protected var source: util.Map[String, AnyRef] = _

  protected def ensureNotNull(obj: Any, errorMsg: String): Unit = if (obj == null) {
    throw new UJESJobException(s"$errorMsg cannot be null.")
  }

  protected def nullThenSet(obj: Any)(setTo: => Unit): Unit = if (obj == null) setTo

  def addExecuteUser(executeUser: String): this.type = {
    this.executeUser = executeUser
    this
  }

  def setJobContent(jobContent: util.Map[String, AnyRef]): this.type = {
    this.jobContent = jobContent
    this
  }

  def addJobContent(key: String, value: AnyRef): this.type = {
    if (jobContent == null) jobContent = new util.HashMap[String, AnyRef]
    jobContent.put(key, value)
    this
  }

  def setLabels(labels: util.Map[String, AnyRef]): this.type = {
    this.labels = labels
    this
  }

  def addLabel(key: String, value: AnyRef): this.type = {
    if (labels == null) labels = new util.HashMap[String, AnyRef]
    labels.put(key, value)
    this
  }

  def setParams(params: util.Map[String, AnyRef]): this.type = {
    this.synchronized(this.params = params)
    this
  }

  def setSource(source: util.Map[String, AnyRef]): this.type = {
    this.synchronized(this.source = source)
    this
  }

  def addSource(key: String, value: AnyRef): this.type = {
    if (source == null) source = new util.HashMap[String, AnyRef]
    source.put(key, value)
    this
  }

  def setStartupParams(startupMap: util.Map[String, AnyRef]): this.type = {
    if (this.params == null) this synchronized {
      if (this.params == null) this.params = new util.HashMap[String, AnyRef]
    }
    TaskUtils.addStartupMap(this.params, startupMap)
    this
  }

  def addStartupParam(key: String, value: AnyRef): this.type =
    addToMap(key, value, setStartupParams)

  def setRuntimeParams(runtimeMap: util.Map[String, AnyRef]): this.type = {
    if (this.params == null) this synchronized {
      if (this.params == null) this.params = new util.HashMap[String, AnyRef]
    }
    TaskUtils.addRuntimeMap(this.params, runtimeMap)
    this
  }

  def addRuntimeParam(key: String, value: AnyRef): this.type =
    addToMap(key, value, setRuntimeParams)

  def setVariableMap(variableMap: util.Map[String, AnyRef]): this.type = {
    if (this.params == null) this synchronized {
      if (this.params == null) this.params = new util.HashMap[String, AnyRef]
    }
    TaskUtils.addVariableMap(this.params, variableMap)
    this
  }

  def addVariable(key: String, value: AnyRef): this.type = addToMap(key, value, setVariableMap)

  protected def addToMap(
      key: String,
      value: AnyRef,
      op: util.Map[String, AnyRef] => this.type
  ): this.type = {
    val map = new util.HashMap[String, AnyRef]
    map.put(key, value)
    op(map)
  }

  def build(): Job

}

abstract class AbstractLinkisJobBuilder[Job <: LinkisJob] extends LinkisJobBuilder[Job] {

  private var ujesClient: UJESClient = _

  def setUJESClient(ujesClient: UJESClient): this.type = {
    this.ujesClient = ujesClient
    this
  }

  protected def createJobSubmitAction(): JobSubmitAction = JobSubmitAction
    .builder()
    .setExecutionContent(jobContent)
    .addExecuteUser(executeUser)
    .setLabels(labels)
    .setParams(params)
    .setSource(source)
    .setUser(executeUser)
    .build()

  protected def createLinkisJob(ujesClient: UJESClient, jobSubmitAction: JobSubmitAction): Job

  protected def validate(): Unit = ensureNotNull(labels, "labels")

  override def build(): Job = {
    validate()
    val jobSubmitAction = createJobSubmitAction()
    if (ujesClient == null) ujesClient = LinkisJobBuilder.getDefaultUJESClient
    createLinkisJob(ujesClient, jobSubmitAction)
  }

}

object LinkisJobBuilder {

  private var clientConfig: DWSClientConfig = _
  private var ujesClient: UJESClient = _
  private var threadPool: ScheduledThreadPoolExecutor = Utils.defaultScheduler
  private var serverUrl: String = _

  private var authTokenValue: String = CommonVars[String](
    "wds.linkis.client.test.common.tokenValue",
    "LINKIS_CLI_TEST"
  ).getValue // This is the default authToken, we usually suggest set different ones for users.

  def setDefaultClientConfig(clientConfig: DWSClientConfig): Unit = this.clientConfig = clientConfig

  def getDefaultClientConfig: DWSClientConfig = {
    if (clientConfig == null) synchronized {
      if (clientConfig == null) buildDefaultConfig()
    }
    clientConfig
  }

  def setDefaultUJESClient(ujesClient: UJESClient): Unit = this.ujesClient = ujesClient

  def getDefaultServerUrl: String = {
    if (StringUtils.isEmpty(serverUrl)) {
      serverUrl = Configuration.getGateWayURL()
      if (StringUtils.isEmpty(serverUrl)) {
        throw new UJESClientBuilderException("serverUrl must be set!")
      }
    }
    serverUrl
  }

  def setDefaultServerUrl(serverUrl: String): Unit = this.serverUrl = serverUrl

  def setDefaultAuthToken(authTokenValue: String): Unit = this.authTokenValue = authTokenValue

  private[client] def justGetDefaultUJESClient: UJESClient = ujesClient

  def getDefaultUJESClient: UJESClient = {
    if (ujesClient == null) synchronized {
      if (clientConfig == null) buildDefaultConfig()
      if (ujesClient == null) {
        ujesClient = new UJESClientImpl(clientConfig)
      }
    }
    ujesClient
  }

  private def buildDefaultConfig(): Unit = {
    val retryHandler = new RetryHandler {}
    retryHandler.addRetryException(classOf[LinkisRetryException])
    clientConfig = DWSClientConfigBuilder
      .newBuilder()
      .addServerUrl(getDefaultServerUrl)
      .connectionTimeout(45000)
      .discoveryEnabled(false)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(false)
      .maxConnectionSize(10)
      .retryEnabled(true)
      .setRetryHandler(retryHandler)
      .readTimeout(90000) // We think 90s is enough, if SocketTimeoutException is throw, just set a new clientConfig to modify it.
      .setAuthenticationStrategy(new TokenAuthenticationStrategy())
      .setAuthTokenKey(TokenAuthenticationStrategy.TOKEN_KEY)
      .setAuthTokenValue(authTokenValue)
      .setDWSVersion(Configuration.LINKIS_WEB_VERSION.getValue)
      .build()
  }

  def setThreadPoolExecutor(threadPool: ScheduledThreadPoolExecutor): Unit = this.threadPool =
    threadPool

  def getThreadPoolExecutor: ScheduledThreadPoolExecutor = threadPool

}
