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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.service

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.ExecutorLockListener
import com.webank.wedatasphere.linkis.manager.common.protocol.{RequestEngineLock, RequestEngineUnlock, ResponseEngineLock, ResponseEngineUnlock}
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils

trait LockService extends ExecutorLockListener with Logging {

  def isLockExist(lock: String): Boolean

  def tryLock(requestEngineLock: RequestEngineLock): Option[String]

  /**
    * Unlock(解锁)
    *
    * @param lock
    */
  def unlock(lock: String): Boolean

  def requestLock(requestEngineLock: RequestEngineLock): ResponseEngineLock = {
    var response: ResponseEngineLock = null
    tryLock(requestEngineLock) match {
      case Some(lockStr) =>
        // Engine can be locked
        if (!StringUtils.isBlank(lockStr)) {
          // lock success
          response = ResponseEngineLock(true, lockStr, s"Lock for ${requestEngineLock.timeout} ms")
        } else {
          // lock failed
          response = ResponseEngineLock(false, lockStr, "lock str is blank")
        }
      case None =>
        // Engine is busy
        response = ResponseEngineLock(false, null, "Engine is busy.")
    }
    info ("RequestLock : " + BDPJettyServerHelper.gson.toJson(requestEngineLock) + "\nResponseLock : " + BDPJettyServerHelper.gson.toJson(response))
    response
  }

  def requestUnLock(requestEngineUnlock: RequestEngineUnlock): ResponseEngineUnlock

}

