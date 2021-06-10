package com.webank.wedatasphere.linkis.manager.common.protocol

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineLockType
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


trait EngineLock extends RequestProtocol

case class RequestEngineLock(timeout: Long = -1, lockType: EngineLockType = EngineLockType.Timed) extends EngineLock

case class RequestEngineUnlock(lock: String) extends EngineLock

case class ResponseEngineLock(lockStatus: Boolean, lock: String, msg: String) extends EngineLock

case class RequestManagerUnlock(engineInstance: ServiceInstance, lock: String, clientInstance: ServiceInstance) extends EngineLock

case class ResponseEngineUnlock(unlocked: Boolean)