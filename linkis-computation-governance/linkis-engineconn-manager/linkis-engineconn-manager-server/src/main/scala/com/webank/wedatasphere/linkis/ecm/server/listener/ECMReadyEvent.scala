/*
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
 */

package com.webank.wedatasphere.linkis.ecm.server.listener

import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.ecm.core.listener.ECMEvent
import com.webank.wedatasphere.linkis.governance.common.protocol.task.ResponseEngineConnPid
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.protocol.callback.{YarnAPPIdCallbackProtocol, YarnInfoCallbackProtocol}


case class ECMReadyEvent(params: Array[String]) extends ECMEvent

case class ECMClosedEvent() extends ECMEvent

case class EngineConnStatusChageEvent(from: NodeStatus, to: NodeStatus) extends ECMEvent

case class YarnAppIdCallbackEvent(protocol: YarnAPPIdCallbackProtocol) extends ECMEvent

case class YarnInfoCallbackEvent(protocol: YarnInfoCallbackProtocol) extends ECMEvent

case class EngineConnPidCallbackEvent(protocol: ResponseEngineConnPid) extends ECMEvent

case class EngineConnAddEvent(conn: EngineConn) extends ECMEvent

case class EngineConnStatusChangeEvent(tickedId: String, updateStatus: NodeStatus) extends ECMEvent


