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

package org.apache.linkis.ujes.client

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.httpclient.response.Result
import org.apache.linkis.ujes.client.request._
import org.apache.linkis.ujes.client.request.JobExecIdAction.JobServiceType
import org.apache.linkis.ujes.client.response.{EncryptTokenResult, _}
import org.apache.linkis.ujes.client.utils.UJESClientUtils

import java.io.Closeable
import java.util
import java.util.concurrent.TimeUnit

abstract class UJESClient extends Closeable with Logging {

  def execute(jobExecuteAction: JobExecuteAction): JobExecuteResult = executeUJESJob(
    jobExecuteAction
  ).asInstanceOf[JobExecuteResult]

  def submit(jobSubmitAction: JobSubmitAction): JobSubmitResult =
    executeUJESJob(jobSubmitAction).asInstanceOf[JobSubmitResult]

  def executeUJESJob(ujesJobAction: UJESJobAction): Result

  private def executeJobExecIdAction[T](
      jobExecuteResult: JobExecuteResult,
      jobServiceType: JobServiceType.JobServiceType
  ): T = {
    val jobExecIdAction = JobExecIdAction
      .builder()
      .setJobServiceType(jobServiceType)
      .setExecId(jobExecuteResult.getExecID)
      .setUser(jobExecuteResult.getUser)
      .build()
    executeUJESJob(jobExecIdAction).asInstanceOf[T]
  }

  /**
   * only get the status of the cache Task status should be based on getJobInfo
   * @param jobExecuteResult
   * @return
   */
  def status(jobExecuteResult: JobExecuteResult): JobStatusResult =
    executeJobExecIdAction(jobExecuteResult, JobServiceType.JobStatus)

  /**
   * IF exception return null progress result
   * @param jobExecuteResult
   * @return
   */
  def progress(jobExecuteResult: JobExecuteResult): JobProgressResult =
    Utils.tryCatch(
      executeJobExecIdAction(jobExecuteResult, JobServiceType.JobProgress)
        .asInstanceOf[JobProgressResult]
    ) { t =>
      logger.warn("Failed to get progress, return empty progress.", t)
      val result = new JobProgressResult
      result.setProgress(0)
      result
    }

  /**
   * If exception return null log
   * @param jobExecuteResult
   * @param fromLine
   * @param size
   * @return
   */
  def log(jobExecuteResult: JobExecuteResult, fromLine: Int, size: Int): JobLogResult = {
    val jobLogAction = JobLogAction
      .builder()
      .setExecId(jobExecuteResult.getExecID)
      .setUser(jobExecuteResult.getUser)
      .setFromLine(fromLine)
      .setSize(size)
      .build()

    Utils.tryCatch(executeUJESJob(jobLogAction).asInstanceOf[JobLogResult]) { t =>
      logger.warn("Failed to get Log, return empty log.", t)
      null
    }
  }

  /**
   * If exception return null log
   * @param jobExecuteResult
   * @param jobLogResult
   * @return
   */
  def log(jobExecuteResult: JobExecuteResult, jobLogResult: JobLogResult): JobLogResult = {
    val jobLogAction = JobLogAction
      .builder()
      .setExecId(jobExecuteResult.getExecID)
      .setUser(jobExecuteResult.getUser)
      .setFromLine(jobLogResult.getFromLine)
      .build()

    Utils.tryCatch(executeUJESJob(jobLogAction).asInstanceOf[JobLogResult]) { t =>
      logger.warn("Failed to get Log, return empty log.", t)
      null
    }
  }

  def openLog(openLogAction: OpenLogAction): OpenLogResult = {
    executeUJESJob(openLogAction).asInstanceOf[OpenLogResult]
  }

  def list(jobListAction: JobListAction): JobListResult = {
    executeUJESJob(jobListAction).asInstanceOf[JobListResult]
  }

  def kill(jobExecuteResult: JobExecuteResult): JobKillResult =
    executeJobExecIdAction(jobExecuteResult, JobServiceType.JobKill)

  def pause(jobExecuteResult: JobExecuteResult): JobPauseResult =
    executeJobExecIdAction(jobExecuteResult, JobServiceType.JobPause)

  def getJobInfo(jobExecuteResult: JobExecuteResult): JobInfoResult = {
    val jobInfoAction = JobInfoAction.builder().setTaskId(jobExecuteResult).build()
    executeUJESJob(jobInfoAction).asInstanceOf[JobInfoResult]
  }

  def resultSet(resultSetAction: ResultSetAction): ResultSetResult =
    executeUJESJob(resultSetAction).asInstanceOf[ResultSetResult]

  def getDBS(getDBSAction: GetDBSAction): GetDBSResult = {
    executeUJESJob(getDBSAction).asInstanceOf[GetDBSResult]
  }

  def getTables(getTableAction: GetTablesAction): GetTablesResult = {
    executeUJESJob(getTableAction).asInstanceOf[GetTablesResult]
  }

  def getColumns(getColumnsAction: GetColumnsAction): GetColumnsResult = {
    executeUJESJob(getColumnsAction).asInstanceOf[GetColumnsResult]
  }

  def getTableStatisticInfo(
      getTableStatisticInfoAction: GetTableStatisticInfoAction
  ): GetTableStatisticInfoResult = {
    executeUJESJob(getTableStatisticInfoAction).asInstanceOf[GetTableStatisticInfoResult]
  }

  def getTableBaseInfo(getTableBaseInfoAction: GetTableBaseInfoAction): GetTableBaseInfoResult = {
    executeUJESJob(getTableBaseInfoAction).asInstanceOf[GetTableBaseInfoResult]
  }

  def getPartitionStatisticInfo(
      getPartitionStatisticInfoAction: GetPartitionStatisticInfoAction
  ): GetPartitionStatisticInfoResult = {
    executeUJESJob(getPartitionStatisticInfoAction).asInstanceOf[GetPartitionStatisticInfoResult]
  }

  def listECM(emsListAction: EmsListAction): EmsListResult = {
    executeUJESJob(emsListAction).asInstanceOf[EmsListResult]
  }

  def addJobObserve(jobObserveAction: JobObserveAction): JobObserveResult = {
    executeUJESJob(jobObserveAction).asInstanceOf[JobObserveResult]
  }

  def deleteJobObserve(jobDeleteObserveAction: JobDeleteObserveAction): JobDeleteObserveResult = {
    executeUJESJob(jobDeleteObserveAction).asInstanceOf[JobDeleteObserveResult]
  }

  def getEncryptToken(token: String, user: String): String = {
    val encryptTokenResult = executeUJESJob(
      EncryptTokenAction.newBuilder().setUser(user).setToken(token).build()
    ).asInstanceOf[EncryptTokenResult]
    encryptTokenResult.encryptToken
  }

}

object UJESClient {
  def apply(clientConfig: DWSClientConfig): UJESClient = new UJESClientImpl(clientConfig)

  def apply(serverUrl: String): UJESClient = apply(serverUrl, 30000, 10)

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int): UJESClient =
    apply(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def apply(
      serverUrl: String,
      readTimeout: Int,
      maxConnection: Int,
      authenticationStrategy: AuthenticationStrategy,
      dwsVersion: String
  ): UJESClient = {
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

  def getDiscoveryClient(serverUrl: String): UJESClient = getDiscoveryClient(serverUrl, 30000, 10)

  def getDiscoveryClient(serverUrl: String, readTimeout: Int, maxConnection: Int): UJESClient =
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
  ): UJESClient = {
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
