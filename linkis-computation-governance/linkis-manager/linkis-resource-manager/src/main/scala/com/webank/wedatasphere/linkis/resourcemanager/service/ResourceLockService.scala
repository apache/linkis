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

package com.webank.wedatasphere.linkis.resourcemanager.service

import java.util.Date

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLock
import com.webank.wedatasphere.linkis.manager.persistence.LockManagerPersistence
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ResourceLockService extends Logging {

  val DEFAULT_LOCKED_BY = "RM"
  @Autowired
  var lockManagerPersistence : LockManagerPersistence = _

  def tryLock(labelContainer: RMLabelContainer): Boolean = tryLock(labelContainer, Long.MaxValue)

  def tryLock(labelContainer: RMLabelContainer, timeout: Long): Boolean = {
    val lockedBy = if(labelContainer.getUserCreatorLabel == null) DEFAULT_LOCKED_BY else labelContainer.getUserCreatorLabel.getUser
    val persistenceLock = new PersistenceLock
    persistenceLock.setLockObject(labelContainer.getCurrentLabel.getStringValue)
    persistenceLock.setCreateTime(new Date)
    persistenceLock.setCreator(lockedBy)
    persistenceLock.setUpdateTime(new Date)
    persistenceLock.setUpdator(lockedBy)
    try {
      lockManagerPersistence.lock(persistenceLock, timeout)
    } catch {
      case t: Throwable =>
        error(s"failed to lock label [${persistenceLock.getLockObject}]", t)
        return false
    }
    labelContainer.getLockedLabels.add(labelContainer.getCurrentLabel)
    return true
  }

  def unLock(labelContainer: RMLabelContainer): Unit = {
    val labelIterator = labelContainer.getLockedLabels.iterator
    while(labelIterator.hasNext){
      val label = labelIterator.next
      val persistenceLock = new PersistenceLock
      persistenceLock.setLockObject(label.getStringValue)
      try {
        lockManagerPersistence.unlock(persistenceLock)
      }catch {
        case t: Throwable =>
          error(s"failed to unlock label [${persistenceLock.getLockObject}]", t)
          throw t
      }
      labelIterator.remove
    }
  }

}
