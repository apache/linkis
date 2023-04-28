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

package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.listener.ExecutorLockListener
import org.apache.linkis.manager.common.protocol.{
  RequestEngineLock,
  RequestEngineUnlock,
  ResponseEngineLock,
  ResponseEngineUnlock
}
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

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
          response =
            new ResponseEngineLock(true, lockStr, s"Lock for ${requestEngineLock.getTimeout} ms")
        } else {
          // lock failed
          response = new ResponseEngineLock(false, lockStr, "lock str is blank")
        }
      case None =>
        // Engine is busy
        response = new ResponseEngineLock(false, null, "Engine is busy.")
    }
    logger.info(
      "RequestLock : " + BDPJettyServerHelper.gson.toJson(
        requestEngineLock
      ) + "\nResponseLock : " + BDPJettyServerHelper.gson.toJson(response)
    )
    response
  }

  def requestUnLock(requestEngineUnlock: RequestEngineUnlock): ResponseEngineUnlock

}
