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

package com.webank.wedatasphere.linkis.entrance.execute.impl

import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration._
import com.webank.wedatasphere.linkis.entrance.execute.{EngineBuilder, EntranceEngine}
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceGroupFactory
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, RequestEngineStatus, ResponseEngineInfo, ResponseEngineStatus}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory


/**
  * Created by enjoyyin on 2018/9/26.
  */
abstract class AbstractEngineBuilder(groupFactory: GroupFactory) extends EngineBuilder with Logging {
  private val idGenerator = new AtomicLong(0)

  protected def createEngine(id: Long): EntranceEngine

  override def buildEngine(instance: String): EntranceEngine = {
    val sender = Sender.getSender(ServiceInstance(ENGINE_SPRING_APPLICATION_NAME.getValue, instance))
    sender.ask(RequestEngineStatus(RequestEngineStatus.Status_Overload_Concurrent)) match {
      case r: ResponseEngineStatus => buildEngine(r)
      case warn: WarnException => throw warn
    }
  }

  protected def getGroupName(creator: String, user: String): String = EntranceGroupFactory.getGroupName(creator, user)

  override def buildEngine(instance: String, requestEngine: RequestEngine): EntranceEngine = {
    val engine = createEngine(idGenerator.incrementAndGet())
    engine.setInstance(instance)
    engine.setGroup(groupFactory.getOrCreateGroup(getGroupName(requestEngine.creator, requestEngine.user)))
    engine.setUser(requestEngine.user)
    engine.setCreator(requestEngine.creator)
    engine.updateState(ExecutorState.Starting, ExecutorState.Idle, null, null)
    engine
  }

  override def buildEngine(responseEngineStatus: ResponseEngineStatus): EntranceEngine = responseEngineStatus  match {
    case ResponseEngineStatus(instance, state, overload, concurrent, ResponseEngineInfo(createEntranceInstance, creator, user, _)) =>
      warn(s"receive a new engine $instance for creator $creator, which is created by other entrance $createEntranceInstance.")
      val engine = createEngine(idGenerator.incrementAndGet())
      engine.setInstance(instance)
      engine.setGroup(groupFactory.getOrCreateGroup(getGroupName(creator, user)))
      engine.setUser(user)
      engine.setCreator(creator)
      engine.updateState(ExecutorState.Starting, ExecutorState.apply(state), overload, concurrent)
      engine
  }
}
