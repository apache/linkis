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

package org.apache.linkis.governance.common.protocol.task

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.RetryableProtocol
import org.apache.linkis.protocol.message.RequestProtocol

import java.util

trait RequestTask {
  def getCode: String
  def setCode(code: String): Unit
  def getLock: String
  def setLock(lock: String): Unit
  def getProperties: util.Map[String, Object]
  def setProperties(properties: util.Map[String, Object])
  def data(key: String, value: Object): Unit
  def getLabels: util.List[Label[_]]
  def setLabels(labels: util.List[Label[_]])
  def getSourceID(): String
}

object RequestTask {
  private val header = "#rt_"
  val RESULT_SET_STORE_PATH: String = header + "rs_store_path"
}

class RequestTaskExecute extends RequestTask with RequestProtocol {
  private var code: String = _
  private var lock: String = _
  private var properties: util.Map[String, Object] = new util.HashMap[String, Object]

  private var labels: util.List[Label[_]] = new util.ArrayList[Label[_]](4)

  private var sourceID: String = _

  override def getCode: String = code

  override def setCode(code: String): Unit = this.code = code

  override def getLock: String = lock

  override def setLock(lock: String): Unit = this.lock = lock

  override def getProperties: util.Map[String, Object] = properties

  override def setProperties(properties: util.Map[String, Object]): Unit = this.properties =
    properties

  override def data(key: String, value: Object): Unit = {
    if (properties == null) properties = new util.HashMap[String, Object]
    properties.put(key, value)
  }

  override def getLabels: util.List[Label[_]] = labels

  override def setLabels(labels: util.List[Label[_]]): Unit = this.labels = labels

  private def getCodeByLimit(num: Int = 50): String = {
    if (code.size > num) {
      code.substring(0, num)
    } else {
      code
    }
  }

  override def toString: String =
    s"RequestTaskExecute(code=${getCodeByLimit()}, lock=$lock, properties=$properties, labels=$labels, sourceID=${getSourceID()})"

  override def getSourceID(): String = sourceID

  def setSourceID(sourceID: String): Unit = this.sourceID = sourceID
}

trait TaskState extends RequestProtocol {}

case class RequestTaskPause(execId: String) extends TaskState
case class RequestTaskResume(execId: String) extends TaskState
case class RequestTaskKill(execId: String) extends TaskState with RetryableProtocol

/**
 * The status of requesting job execution, mainly used for:<br>
 *   1. If the engine has not sent information to the entity for a long time, the enumeration
 *      periodically requests status information<br> 2. If the entance of the request engine is
 *      hanged, the newly taken entity first requests the status of the job from the engine, the
 *      engine maintains the new sender information, and then sends log information to the sender.
 *      请求Job执行的状态，主要用于：<br>
 *   1. 如果engine很久没有给entrance发送信息，entrance定时请求一次状态信息<br> 2.
 *      如果请求engine的entrance挂掉了，新接手的entrance第一次向engine请求Job的状态，engine维系新的sender信息，后续向该sender发送日志信息
 */
case class RequestTaskStatus(execId: String) extends TaskState
