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
 
package org.apache.linkis.resourcemanager.service

import java.util.Date

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.persistence.PersistenceLock
import org.apache.linkis.manager.label.entity.{EngineNodeLabel, ResourceLabel}
import org.apache.linkis.manager.persistence.LockManagerPersistence
import org.apache.linkis.resourcemanager.domain.RMLabelContainer
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class ResourceLockService extends Logging {

  val DEFAULT_LOCKED_BY = "RM"
  @Autowired
  var lockManagerPersistence : LockManagerPersistence = _

  def tryLock(labelContainer: RMLabelContainer): Boolean = tryLock(labelContainer, Long.MaxValue)

  def tryLock(labelContainer: RMLabelContainer, timeout: Long): Boolean = {
    if (StringUtils.isBlank(labelContainer.getCurrentLabel.getStringValue)
      || !labelContainer.getCurrentLabel.isInstanceOf[ResourceLabel]
      || labelContainer.getLockedLabels.contains(labelContainer.getCurrentLabel)) {
      return true
    }
    val lockedBy = if (labelContainer.getUserCreatorLabel == null) DEFAULT_LOCKED_BY else labelContainer.getUserCreatorLabel.getUser
    val persistenceLock = new PersistenceLock
    persistenceLock.setLockObject(labelContainer.getCurrentLabel.getStringValue)
    persistenceLock.setCreateTime(new Date)
    persistenceLock.setCreator(lockedBy)
    persistenceLock.setUpdateTime(new Date)
    persistenceLock.setUpdator(lockedBy)
    try {
      val isLocked: Boolean = if (timeout > 0) {
        lockManagerPersistence.lock(persistenceLock, timeout)
      } else {
        lockManagerPersistence.lock(persistenceLock, Long.MaxValue)
      }
      if(isLocked) {
        info(labelContainer.getCurrentLabel + " successfully locked label" + persistenceLock.getLockObject)
        labelContainer.getLockedLabels.add(labelContainer.getCurrentLabel)
      }
      isLocked
    } catch {
      case t: Throwable =>
        error(s"failed to lock label [${persistenceLock.getLockObject}]", t)
        false
    }
  }

  def unLock(labelContainer: RMLabelContainer): Unit = {
    val labelIterator = labelContainer.getLockedLabels.iterator
    while(labelIterator.hasNext) {
      val label = labelIterator.next
      if (!StringUtils.isBlank(label.getStringValue)) {
        val persistenceLock = new PersistenceLock
        persistenceLock.setLockObject(label.getStringValue)
        try {
          lockManagerPersistence.unlock(persistenceLock)
          info("unlocked " + persistenceLock.getLockObject)
        } catch {
          case t: Throwable =>
            error(s"failed to unlock label [${persistenceLock.getLockObject}]", t)
            throw t
        }
        labelIterator.remove
      }
    }
  }

  def clearTimeoutLock(timeout: Long): Unit = {
    val currentTime = System.currentTimeMillis
    lockManagerPersistence.getAll.foreach{ lock =>
      if (currentTime - lock.getCreateTime.getTime > timeout) {
        lockManagerPersistence.unlock(lock)
        warn("timeout force unlock " + lock.getLockObject)
      }
    }
  }

}
