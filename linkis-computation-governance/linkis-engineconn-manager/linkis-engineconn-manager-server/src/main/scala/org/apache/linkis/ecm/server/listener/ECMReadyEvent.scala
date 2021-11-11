/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.ecm.server.listener

import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.listener.ECMEvent
import org.apache.linkis.governance.common.protocol.task.ResponseEngineConnPid
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.protocol.callback.{YarnAPPIdCallbackProtocol, YarnInfoCallbackProtocol}


case class ECMReadyEvent(params: Array[String]) extends ECMEvent

case class ECMClosedEvent() extends ECMEvent

case class EngineConnStatusChageEvent(from: NodeStatus, to: NodeStatus) extends ECMEvent

case class YarnAppIdCallbackEvent(protocol: YarnAPPIdCallbackProtocol) extends ECMEvent

case class YarnInfoCallbackEvent(protocol: YarnInfoCallbackProtocol) extends ECMEvent

case class EngineConnPidCallbackEvent(protocol: ResponseEngineConnPid) extends ECMEvent

case class EngineConnAddEvent(conn: EngineConn) extends ECMEvent

case class EngineConnStatusChangeEvent(tickedId: String, updateStatus: NodeStatus) extends ECMEvent


