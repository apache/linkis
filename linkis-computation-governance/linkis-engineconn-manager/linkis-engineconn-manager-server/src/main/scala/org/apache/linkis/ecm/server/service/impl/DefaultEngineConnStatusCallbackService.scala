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

package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.conf.ECMConfiguration.MANAGER_SERVICE_NAME
import org.apache.linkis.ecm.server.listener.EngineConnStatusChangeEvent
import org.apache.linkis.ecm.server.service.EngineConnStatusCallbackService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus.{Failed, Running}
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnStatusCallback,
  EngineConnStatusCallbackToAM
}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.stereotype.Service

@Service
class DefaultEngineConnStatusCallbackService extends EngineConnStatusCallbackService with Logging {

  @Receiver
  override def dealEngineConnStatusCallback(protocol: EngineConnStatusCallback): Unit = {
    logger.info(s"Start to deal EngineConnStatusCallback $protocol")

    if (NodeStatus.isAvailable(protocol.status)) {

      LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(
        EngineConnStatusChangeEvent(protocol.ticketId, Running)
      )
    } else {

      Sender
        .getSender(MANAGER_SERVICE_NAME)
        .send(
          EngineConnStatusCallbackToAM(
            protocol.serviceInstance,
            protocol.status,
            protocol.initErrorMsg
          )
        )
      LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(
        EngineConnStatusChangeEvent(protocol.ticketId, Failed)
      )
    }

    logger.info(s"Finished to deal EngineConnStatusCallback $protocol")
  }

}
