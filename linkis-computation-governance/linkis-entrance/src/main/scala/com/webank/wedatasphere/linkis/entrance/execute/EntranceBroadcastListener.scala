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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
import com.webank.wedatasphere.linkis.protocol.BroadcastProtocol
import com.webank.wedatasphere.linkis.protocol.engine.{BroadcastNewEngine, ResponseEngineStatusChanged}
import com.webank.wedatasphere.linkis.rpc.{BroadcastListener, Sender}
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState

/**
  * Created by enjoyyin on 2019/2/12.
  */
trait EntranceBroadcastListener extends BroadcastListener with Logging {

  @EntranceExecutorManagerAutowiredAnnotation
  private var entranceExecutorManager: EntranceExecutorManager = _

  def setEntranceExecutorManager(entranceExecutorManager: EntranceExecutorManager): Unit = this.entranceExecutorManager = entranceExecutorManager
  def getEntranceExecutorManager: EntranceExecutorManager = entranceExecutorManager

}

class NewEngineBroadcastListener extends EntranceBroadcastListener {
  override def onBroadcastEvent(protocol: BroadcastProtocol, sender: Sender): Unit = protocol match {
    case BroadcastNewEngine(_, responseEngineStatus) =>
      info(s"received a new broadcast engine $responseEngineStatus.")
      if(getEntranceExecutorManager.getOrCreateEngineManager().get(responseEngineStatus.instance).isEmpty) {
        val engine = getEntranceExecutorManager.getOrCreateEngineBuilder().buildEngine(responseEngineStatus)
        getEntranceExecutorManager.initialEntranceEngine(engine)
      }
    case _ =>
  }
}

class ResponseEngineStatusChangedBroadcastListener extends EntranceBroadcastListener {
  override def onBroadcastEvent(protocol: BroadcastProtocol, sender: Sender): Unit = protocol match {
    case ResponseEngineStatusChanged(instance, fromState, toState, overload, concurrent) =>
      val from = ExecutorState(fromState)
      val to = ExecutorState(toState)
      info(s"received the broadcast state from $from to $to for engine instance $instance.")
      getEntranceExecutorManager.getOrCreateEngineManager().get(instance).foreach { engine =>
        engine.updateState(from, to, overload, concurrent)
      }
    case _ =>
  }
}