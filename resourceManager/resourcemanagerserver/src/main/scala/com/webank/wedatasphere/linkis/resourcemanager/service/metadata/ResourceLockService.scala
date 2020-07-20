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

package com.webank.wedatasphere.linkis.resourcemanager.service.metadata

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.dao.ResourceLockDao
import com.webank.wedatasphere.linkis.resourcemanager.domain.ResourceLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.{DataAccessException, DeadlockLoserDataAccessException, DuplicateKeyException}
import org.springframework.stereotype.Component

/**
  * Created by shanhuang on 9/11/18.
  */
@Component
class ResourceLockService extends Logging {

  @Autowired
  var resourceLockDao: ResourceLockDao = _

  def tryLock(user: String, emApplicationName: String, emInstance: String): Boolean = tryLock(user, emApplicationName, emInstance, Long.MaxValue)

  def tryLock(user: String, emApplicationName: String, emInstance: String, timeout: Long): Boolean = {
    var realUser = ""
    if (user != null) realUser = user
    val startTime = System.currentTimeMillis
    val resourceLock = new ResourceLock(realUser, emApplicationName, emInstance)
    while (!tryInsert(resourceLock) && System.currentTimeMillis - startTime < timeout) Thread.sleep(5)
    System.currentTimeMillis - startTime < timeout
  }

  def unLock(user: String, emApplicationName: String, emInstance: String): Boolean = {
    var realUser = ""
    if (user != null) realUser = user
    val existing = resourceLockDao.getLock(realUser, emApplicationName, emInstance)
    if (existing == null) return false
    resourceLockDao.deleteById(existing.getId)
    true
  }

  //  private def waitForLock(user: String, emApplicationName: String, emInstance: String, timeout: Long, unit: TimeUnit): Boolean = {
  //    while(resourceLockDao.getLock(user, emApplicationName, emInstance) != null){
  //      Thread.sleep(5)
  //    }
  //    true
  //  }

  private def tryInsert(resourceLock: ResourceLock) = try {
    resourceLockDao.insert(resourceLock)
    true
  } catch {
    case e: MySQLIntegrityConstraintViolationException => false
    case d: DuplicateKeyException => false
    case de: DeadlockLoserDataAccessException => false
    case da: DataAccessException =>
      error("unexpected DataAccessException: ", da)
      false
  }

}
