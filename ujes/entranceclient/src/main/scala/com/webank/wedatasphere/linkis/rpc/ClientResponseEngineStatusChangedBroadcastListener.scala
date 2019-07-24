/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.rpc

import com.webank.wedatasphere.linkis.entrance.execute.{EntranceReceiver, EntranceReceiverUtils, ResponseEngineStatusChangedBroadcastListener}
import com.webank.wedatasphere.linkis.entranceclient.EntranceClientImpl
import com.webank.wedatasphere.linkis.protocol.BroadcastProtocol
import com.webank.wedatasphere.linkis.protocol.engine.ResponseEngineStatusChanged
import com.webank.wedatasphere.linkis.rpc.sender.SpringMVCRPCSender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState

import scala.collection.JavaConversions._

/**
  * Created by johnnwang on 2019/1/22.
  */
class ClientResponseEngineStatusChangedBroadcastListener extends ResponseEngineStatusChangedBroadcastListener {
  override def onBroadcastEvent(protocol: BroadcastProtocol, sender: Sender): Unit = protocol match {
    case ResponseEngineStatusChanged(instance, fromState, toState, overload, concurrent) =>
      sender match {
        case springSender: SpringMVCRPCSender =>
          val serviceInstance = springSender.serviceInstance
          var find = false
          EntranceClientImpl.getClientNames.foreach { clientName =>
            EntranceClientImpl(clientName).getReceiverChooser.foreach(_.chooseReceiver(RPCMessageEvent(protocol, serviceInstance)).foreach{
              case r : EntranceReceiver =>
                find = true
                val from = ExecutorState(fromState)
                val to = ExecutorState(toState)
                info(s"${serviceInstance.getApplicationName}Receiver received the broadcast state from $from to $to for engine instance $instance.")
                EntranceReceiverUtils.getEntranceExecutorManager(r).getOrCreateEngineManager().get(instance).foreach { engine =>
                  engine.updateState(from, to, overload, concurrent)
                }
              case _ =>
            })
          }
          if(!find) super.onBroadcastEvent(protocol, sender)
        case _ => super.onBroadcastEvent(protocol, sender)
      }
    case _ =>
  }
}
