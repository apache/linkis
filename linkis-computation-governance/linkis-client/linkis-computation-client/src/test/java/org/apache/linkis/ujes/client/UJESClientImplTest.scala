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
 
package org.apache.linkis.ujes.client

import java.util.concurrent.TimeUnit

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder
import org.apache.linkis.ujes.client.request.JobExecuteAction.EngineType
import org.apache.linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
import org.apache.commons.io.IOUtils

@Deprecated
object UJESClientImplTest extends App {

  // Suggest to use LinkisJobClient to submit job to Linkis.
  val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl("http://localhost:port")
    .connectionTimeout(30000).discoveryEnabled(true)
    .discoveryFrequency(1, TimeUnit.MINUTES)
    .loadbalancerEnabled(true).maxConnectionSize(5)
    .retryEnabled(false).readTimeout(30000)
    .setAuthenticationStrategy(new StaticAuthenticationStrategy()).setAuthTokenKey("")
    .setAuthTokenValue("").setDWSVersion("v1").build()
  val client = UJESClient(clientConfig)

  val jobExecuteResult = client.execute(JobExecuteAction.builder().setCreator("UJESClient-Test")
    .addExecuteCode("show tables")
    .setEngineType(EngineType.SPARK).setUser("").build())
  println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)
  var status = client.status(jobExecuteResult)
  while(!status.isCompleted) {
    val progress = client.progress(jobExecuteResult)
    val progressInfo = if(progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
    println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
    Utils.sleepQuietly(500)
    status = client.status(jobExecuteResult)
  }
  val jobInfo = client.getJobInfo(jobExecuteResult)
  val resultSet = jobInfo.getResultSetList(client).head
  val fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
  println("fileContents: " + fileContents)
  IOUtils.closeQuietly(client)
}
