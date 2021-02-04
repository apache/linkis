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

package com.webank.wedatasphere.linkis.entranceclient.execute

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.entrance.execute.{ConcurrentEntranceEngine, EntranceEngine, SingleEntranceEngine}
import com.webank.wedatasphere.linkis.entrance.execute.impl.AbstractEngineBuilder
import com.webank.wedatasphere.linkis.entranceclient.EngineApplicationNameFactory
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngineStatus, ResponseEngineStatus}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory

/**
  * Created by johnnwang on 2019/1/22.
  */
class ClientEngineBuilder(groupFactory: GroupFactory) extends AbstractEngineBuilder(groupFactory) with EngineApplicationNameFactory {

  protected val isConcurrentEngineBuilder = true

  override final def buildEngine(instance: String): EntranceEngine = {
    val sender = Sender.getSender(ServiceInstance(getEngineApplicationName, instance))
    sender.ask(RequestEngineStatus(RequestEngineStatus.Status_Overload_Concurrent)) match {
      case r: ResponseEngineStatus => buildEngine(r)
      case warn: WarnException => throw warn
    }
  }

  override final protected def createEngine(id: Long): EntranceEngine = if(isConcurrentEngineBuilder) new ConcurrentEntranceEngine(id) {
    override def setInstance(instance: String): Unit = setServiceInstance(getEngineApplicationName, instance)
  } else new SingleEntranceEngine(id) {
    override def setInstance(instance: String): Unit = setServiceInstance(getEngineApplicationName, instance)
  }
}