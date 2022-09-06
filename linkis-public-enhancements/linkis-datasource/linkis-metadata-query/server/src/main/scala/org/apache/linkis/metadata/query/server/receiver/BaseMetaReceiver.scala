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

package org.apache.linkis.metadata.query.server.receiver

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.metadata.query.common.exception.MetaMethodInvokeException
import org.apache.linkis.metadata.query.common.protocol.{MetadataConnect, MetadataResponse}
import org.apache.linkis.metadata.query.server.service.MetadataQueryService
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.server.BDPJettyServerHelper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.slf4j.LoggerFactory

@Component
class BaseMetaReceiver {

  private val logger = LoggerFactory.getLogger(classOf[BaseMetaReceiver])

  @Autowired
  private var metadataQueryService: MetadataQueryService = _

  @Receiver
  def dealMetadataConnectRequest(metadataConnect: MetadataConnect): MetadataResponse =
    Utils.tryCatch {
      metadataQueryService.getConnection(
        metadataConnect.dataSourceType,
        metadataConnect.operator,
        metadataConnect.params
      )
      MetadataResponse(status = true, BDPJettyServerHelper.gson.toJson(null))
    } {
      case e: WarnException =>
        val errorMsg = e.getMessage
        logger.trace(s"Fail to invoke meta service: [$errorMsg]")
        MetadataResponse(status = false, errorMsg)
      case t: Exception =>
        t match {
          case exception: MetaMethodInvokeException =>
            MetadataResponse(status = false, exception.getCause.getMessage)
          case _ =>
            logger.error(s"Fail to invoke meta service", t)
            MetadataResponse(status = false, t.getMessage)
        }
    }

}
