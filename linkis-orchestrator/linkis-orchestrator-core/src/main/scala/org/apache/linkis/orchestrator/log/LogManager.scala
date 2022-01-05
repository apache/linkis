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
 
package org.apache.linkis.orchestrator.log

import java.util
import java.util.concurrent.ConcurrentHashMap

/**
  *
  *
  */
trait LogManager {

  def cacheLog(jobReqId: String, log: String): Unit

  def getAndClearLog(jobReqId: String): String

}

class DefaultLogManager extends LogManager {

  private val logCache: util.Map[String, StringBuilder] = new ConcurrentHashMap[String, StringBuilder]()

  override def cacheLog(jobReqId: String, log: String): Unit = {
    if (logCache.containsKey(jobReqId)) {
      val sb = logCache.get(jobReqId)
      sb.append("\n" + log)
    } else {
      val sb = new StringBuilder
      sb.append(log)
      logCache.put(jobReqId, sb)
    }
  }

  override def getAndClearLog(jobGroupId: String): String = {
    if (logCache.containsKey(jobGroupId)) {
      val sb = logCache.remove(jobGroupId)
      return sb.toString()
    }
    null
  }

}

object LogManager {

  private val logManager = new DefaultLogManager

  def getLogManager: LogManager = this.logManager

}