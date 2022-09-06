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

package org.apache.linkis.filesystem.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration

import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import java.util
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.JavaConverters.mapAsScalaConcurrentMapConverter
import scala.concurrent.duration.Duration

import com.sun.jna.Platform
import oshi.util.{ExecutingCommand, FileUtil}

@Service
class UserService extends Logging {

  private val userIdMap = new ConcurrentHashMap[String, String]()

  @PostConstruct
  private def init(): Unit = {
    val runnable = new Runnable {
      override def run(): Unit = {
        Utils.tryAndError(initMap)
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(
      runnable,
      1000L,
      WorkSpaceConfiguration.LOCAL_FILESYSTEM_USER_REFRESH_INTERVAL.getValue,
      TimeUnit.MILLISECONDS
    )
  }

  private def initMap(): Unit = {
    val tmpMap = new util.HashMap[String, String]()
    var passwd: util.List[String] = null
    if (Platform.isAIX) passwd = FileUtil.readFile("/etc/passwd")
    else passwd = ExecutingCommand.runNative("getent passwd")
    val var2 = passwd.iterator
    while (var2.hasNext) {
      val entry = var2.next.asInstanceOf[String]
      val split = entry.split(":")
      if (split.length > 2) {
        val userName = split(0)
        val uid = split(2)
        val previousValue = tmpMap.put(userName, uid)
      }
    }
    userIdMap.clear()
    userIdMap.putAll(tmpMap)
    logger.info("UserIdMap inited.")
    if (logger.isDebugEnabled()) {
      logger.debug("inited. " + userIdMap.asScala.map(kv => kv._1 + "->" + kv._2).mkString(","))
    }
  }

  def isUserExist(username: String): Boolean = {
    userIdMap.contains(username)
  }

}
