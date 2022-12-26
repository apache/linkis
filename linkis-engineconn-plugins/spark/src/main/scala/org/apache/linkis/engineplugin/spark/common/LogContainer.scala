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

package org.apache.linkis.engineplugin.spark.common

import scala.collection.Iterable
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 */
class LogContainer(val logSize: Int) {

  private final val logs = new Array[String](logSize)
  private var flag, tail = 0

  def putLog(log: String): Unit = {
    logs.synchronized {
      val index = (tail + 1) % logSize
      if (index == flag) {
        flag = (flag + 1) % logSize
      }
      logs(tail) = log
      tail = index
    }
  }

  def putLogs(logs: Iterable[String]): Unit = synchronized {
    logs.foreach(putLog)
  }

  def reset(): Unit = synchronized {
    flag = 0
    tail = 0
  }

  def getLogs: List[String] = {
    logs.synchronized {
      if (flag == tail) {
        return List.empty[String]
      }
      val _logs = ArrayBuffer[String]()
      val _tail = if (flag > tail) tail + logSize else tail
      for (index <- flag until _tail) {
        val _index = index % logSize
        _logs += logs(_index)
      }
      flag = tail
      _logs.toList
    }
  }

  def size: Int = {
    if (flag == tail) 0
    else if (flag > tail) tail + logSize - flag
    else tail - flag
  }

  def getLogList: java.util.List[String] = getLogs.asJava

}
