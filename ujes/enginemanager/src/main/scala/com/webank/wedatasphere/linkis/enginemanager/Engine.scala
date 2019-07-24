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

package com.webank.wedatasphere.linkis.enginemanager

import com.webank.wedatasphere.linkis.protocol.engine.EngineState.EngineState
import com.webank.wedatasphere.linkis.resourcemanager.Resource

/**
  * Created by johnnwang on 2018/9/6.
  */
abstract class Engine {

  private var resource: Resource = _

  def setResource(resource: Resource): Unit = this.resource = resource
  def getResource = resource

  def getPort: Int
  def init(): Unit
  def shutdown(): Unit

  /**
    * creator(创建者)
    * @return
    */
  def getCreator: String

  /**
    * Actually launched user(实际启动的用户)
    * @return
    */
  def getUser: String
  def getCreateTime: Long
  def getInitedTime: Long
  def getTicketId: String
  def getState: EngineState

  override def toString: String = getClass.getSimpleName + s"(port: $getPort, creator: $getCreator, user: $getUser)"
}