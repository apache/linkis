/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.manager.am.locker

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEngineNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.common.protocol.{RequestEngineLock, RequestEngineUnlock, RequestManagerUnlock}
import com.webank.wedatasphere.linkis.manager.service.common.pointer.NodePointerBuilder
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class DefaultEngineNodeLocker extends EngineNodeLocker with Logging {

  @Autowired
  private var nodeBuilder: NodePointerBuilder = _

  override def lockEngine(engineNode: EngineNode, timeout: Long): Option[String] = {
    //TODO 判断engine需要的锁类型进行不同的实例化
    nodeBuilder.buildEngineNodePointer(engineNode).lockEngine(RequestEngineLock(timeout))
  }


  override def releaseLock(engineNode: EngineNode, lock: String): Unit = {
    nodeBuilder.buildEngineNodePointer(engineNode).releaseLock(RequestEngineUnlock(lock))
  }

  @Receiver
  def releaseLock(requestManagerUnlock: RequestManagerUnlock): Unit = {
    info(s"client${requestManagerUnlock.clientInstance} Start to unlock engine ${requestManagerUnlock.engineInstance}")
    val engineNode = new AMEngineNode()
    engineNode.setServiceInstance(requestManagerUnlock.engineInstance)
    releaseLock(engineNode, requestManagerUnlock.lock)
    info(s"client${requestManagerUnlock.clientInstance} Finished to unlock engine ${requestManagerUnlock.engineInstance}")
  }

}
