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

import org.apache.linkis.bml.client.BmlClientFactory
import org.apache.linkis.computation.client.interactive.{InteractiveJob, InteractiveJobBuilder}
import org.apache.linkis.computation.client.once.{LinkisManagerClient, OnceJob}
import org.apache.linkis.computation.client.once.simple.{SimpleOnceJob, SimpleOnceJobBuilder}
import org.apache.linkis.httpclient.dws.config.DWSClientConfig
import org.apache.linkis.ujes.client.UJESClientImpl

import java.io.Closeable

class LinkisJobClient(clientConfig: DWSClientConfig) extends Closeable {

  private val ujseClient = new UJESClientImpl(clientConfig)

  private lazy val linkisManagerCLient = LinkisManagerClient(ujseClient)

  override def close(): Unit = {
    if (null != linkisManagerCLient) {
      linkisManagerCLient.close()
    }
  }

  def onceJobBuilder(): SimpleOnceJobBuilder =
    SimpleOnceJob.builder(SimpleOnceJobBuilder.getBmlClient(clientConfig), linkisManagerCLient)

  def interactiveJobBuilder(): InteractiveJobBuilder = {
    val builder = InteractiveJob.builder()
    builder.setUJESClient(ujseClient)
  }

}

/**
 * This class is only used to provide a unified entry for user to build a LinkisJob conveniently and
 * simply. Please keep this class lightweight enough, do not set too many field to confuse user.
 */
object LinkisJobClient extends Closeable {

  val config = LinkisJobBuilder

  val interactive = InteractiveJob
  val once = OnceJob

  override def close(): Unit = {
    if (config.justGetDefaultUJESClient != null) {
      config.justGetDefaultUJESClient.close()
    }
  }

}
