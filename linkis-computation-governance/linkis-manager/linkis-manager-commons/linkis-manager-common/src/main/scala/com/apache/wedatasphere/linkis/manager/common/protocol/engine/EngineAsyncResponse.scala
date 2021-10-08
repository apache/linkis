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

package com.apache.wedatasphere.linkis.manager.common.protocol.engine

import com.apache.wedatasphere.linkis.common.ServiceInstance
import com.apache.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.apache.wedatasphere.linkis.protocol.message.RequestProtocol


trait EngineAsyncResponse extends RequestProtocol {
  def id(): String
}

case class EngineAskAsyncResponse(override val id: String, managerInstance: ServiceInstance) extends EngineAsyncResponse

case class EngineCreateSuccess(override val id: String, engineNode: EngineNode) extends EngineAsyncResponse

case class EngineCreateError(override val id: String, exception: String, retry: Boolean = false) extends EngineAsyncResponse

