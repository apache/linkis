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

package org.apache.linkis.manager.rm.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.persistence.PersistenceLock
import org.apache.linkis.manager.persistence.LockManagerPersistence

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.Date

import scala.collection.JavaConverters._

@Component
class ResourceLockService extends Logging {

  val DEFAULT_LOCKED_BY = "RM"

  @Autowired
  var lockManagerPersistence: LockManagerPersistence = _

  def tryLock(persistenceLock: PersistenceLock, timeout: Long): Boolean = {
    if (StringUtils.isBlank(persistenceLock.getLockObject)) {
      return true
    }
    Utils.tryCatch {
      val isLocked: Boolean = if (timeout > 0) {
        lockManagerPersistence.lock(persistenceLock, timeout)
      } else {
        lockManagerPersistence.lock(persistenceLock, Long.MaxValue)
      }
      if (isLocked) {
        logger.info("successfully locked label " + persistenceLock.getLockObject)
      }
      isLocked
    } { case t: Throwable =>
      logger.error(s"Failed to lock label [${persistenceLock.getLockObject}]", t)
      false
    }
  }

  def unLock(persistenceLock: PersistenceLock): Unit = {
    Utils.tryCatch {
      lockManagerPersistence.unlock(persistenceLock)
      logger.info("unlocked " + persistenceLock.getLockObject)
    } { case t: Throwable =>
      logger.error(s"Failed to unlock label [${persistenceLock.getLockObject}]", t)
    }
  }

  /**
   * Clean up the created time before end Date 清理所创建时间在end Date之前的
   * @param timeout
   */
  def clearTimeoutLock(timeout: Long): Unit = {
    val endDate = new Date(System.currentTimeMillis() - timeout)
    val locks = lockManagerPersistence.getTimeOutLocks(endDate)
    if (null == locks) return
    locks.asScala.foreach { lock =>
      Utils.tryAndWarn(lockManagerPersistence.unlock(lock))
      logger.warn("timeout force unlock " + lock.getLockObject)
    }
  }

}
