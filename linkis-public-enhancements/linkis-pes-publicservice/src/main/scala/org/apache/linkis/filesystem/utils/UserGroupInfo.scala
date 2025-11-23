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

package org.apache.linkis.filesystem.utils

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration

import org.apache.commons.lang3.SystemUtils

import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

import scala.collection.mutable.ArrayBuffer

import oshi.util.{ExecutingCommand, FileUtil}

/**
 * TODO If other modules need to be used later, the tool class should be moved to the linkis-storage
 * module
 */

class UserGroupInfo extends Logging {

  private val userList = new ArrayBuffer[String]()

  private val USERLOCKER = new ReentrantReadWriteLock()

  private def init(): Unit = {
    Utils.tryAndError(refreshUserMap())
    val runnable = new Runnable {
      override def run(): Unit = {
        Utils.tryAndError(refreshUserMap())
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(
      runnable,
      100 * 1000L,
      WorkSpaceConfiguration.LOCAL_FILESYSTEM_USER_REFRESH_INTERVAL.getValue,
      TimeUnit.MILLISECONDS
    )

  }

  init()

  private def refreshUserMap(): Unit = {
    val tmpUsers = new ArrayBuffer[String]()
    var passwd: util.List[String] = null
    if (SystemUtils.IS_OS_AIX) passwd = FileUtil.readFile("/etc/passwd")
    else passwd = ExecutingCommand.runNative("getent passwd")
    val iterator = passwd.iterator
    while (iterator.hasNext) {
      val entry = iterator.next.asInstanceOf[String]
      val split = entry.split(":")
      if (split.length > 2) {
        val userName = split(0)
        tmpUsers.append(userName)
      }
    }
    tmpUsers.sorted
    logger.info("user refresh user count {}", tmpUsers.length)
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh users:{}", tmpUsers.mkString(","))
    }
    if (tmpUsers.isEmpty) {
      logger.warn("Refresh user map is empty")
      return
    }
    USERLOCKER.writeLock().lock()
    Utils.tryFinally {
      userList.clear()
      userList ++= tmpUsers
    } {
      USERLOCKER.writeLock().unlock()
    }

  }

  def isUserExist(username: String): Boolean = {
    USERLOCKER.readLock().lock()
    Utils.tryFinally {
      userList.exists(_.equals(username))
    } {
      USERLOCKER.readLock().unlock()
    }
  }

}

object UserGroupUtils {

  private lazy val userGroupInfo = new UserGroupInfo()

  def isUserExist(username: String): Boolean = {
    if (WorkSpaceConfiguration.ENABLE_USER_GROUP.getValue) {
      userGroupInfo.isUserExist(username)
    } else {
      true
    }
  }

}
