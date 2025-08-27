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

package org.apache.linkis.manager.common.protocol

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.protocol.engine.EngineLockType
import org.apache.linkis.protocol.message.RequestProtocol

trait EngineLock extends RequestProtocol

case class RequestEngineLock(timeout: Long = -1, lockType: EngineLockType = EngineLockType.Timed)
    extends EngineLock

case class RequestEngineUnlock(lock: String) extends EngineLock

case class ResponseEngineLock(lockStatus: Boolean, lock: String, msg: String) extends EngineLock

case class RequestManagerUnlock(
    engineInstance: ServiceInstance,
    lock: String,
    clientInstance: ServiceInstance
) extends EngineLock

case class ResponseEngineUnlock(unlocked: Boolean)
