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

package org.apache.linkis.metadatamanager.server.receiver

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.metadatamanager.common.exception.MetaMethodInvokeException
import org.apache.linkis.metadatamanager.common.protocol.{MetadataConnect, MetadataResponse}
import org.apache.linkis.metadatamanager.server.service.MetadataAppService
import org.apache.linkis.rpc.{Receiver, Sender}
import org.apache.linkis.server.BDPJettyServerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

@Component
class BaseMetaReceiver extends Receiver with Logging{
  @Autowired
  private var metadataAppService:MetadataAppService = _


  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = invoke(metadataAppService, message)

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = invoke(metadataAppService, message)


  def invoke(service: MetadataAppService, message: Any): Any = Utils.tryCatch{
    val data = message match {
      case MetadataConnect(dataSourceType, operator, params, version) =>
        service.getConnection(dataSourceType, operator, params)
        // MetadataConnection is not scala class
        null
      case _ => new Object()
    }
    MetadataResponse(status = true, BDPJettyServerHelper.gson.toJson(data))
  }{
    case e: WarnException => val errorMsg = e.getMessage
      trace(s"Fail to invoke meta service: [$errorMsg]")
      MetadataResponse(status = false, errorMsg)
    case t: Exception =>
      t match {
        case exception: MetaMethodInvokeException =>
          MetadataResponse(status = false, exception.getCause.getMessage)
        case _ =>
          error(s"Fail to invoke meta service", t)
          MetadataResponse(status = false, t.getMessage)
      }
  }
}

