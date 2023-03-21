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

import org.apache.commons.io.IOUtils
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.httpclient.dws.authentication.{StaticAuthenticationStrategy, TokenAuthenticationStrategy}
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.ujes.client.request.{EmsListAction, JobExecuteAction, JobObserveAction, ResultSetAction}

import java.util
import java.util.concurrent.TimeUnit

@Deprecated
object JobObserveActionTest extends App {

  val bmlToken = CommonVars("wds.linkis.bml.auth.token.value", "BML-AUTH").getValue

  val clientConfig = DWSClientConfigBuilder.newBuilder()
    .addServerUrl("127.0.0.1:9001") // Change to test gateway address
    .connectionTimeout(30000)
    .discoveryEnabled(false)
    .discoveryFrequency(1, TimeUnit.MINUTES)
    .loadbalancerEnabled(false)
    .maxConnectionSize(5)
    .retryEnabled(false)
    .readTimeout(30000)
    .setAuthenticationStrategy(new TokenAuthenticationStrategy())
    .setAuthTokenKey("Validation-Code")
    .setAuthTokenValue(bmlToken)
    .setDWSVersion("v1")
    .build()
  val client = new UJESClientImpl(clientConfig)
  val map = new util.HashMap[String, String]()
  map.put("title", "ss");
  map.put("detail", "ss");
  val build = JobObserveAction.builder()
    .setUser("hadoop")
    .setTaskId("73695")  // This needs a real ID
    .setSubSystemId("4523")
    .setMonitorLevel("minor")
    .setReceiver("alexyang")
    .setExtra(map)
    .build
  val result = client.addJobObserve(build)
  println(result)
  IOUtils.closeQuietly(client)
}
