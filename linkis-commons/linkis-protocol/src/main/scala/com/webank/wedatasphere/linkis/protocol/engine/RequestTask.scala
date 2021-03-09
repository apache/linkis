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

package com.webank.wedatasphere.linkis.protocol.engine

import java.util

import com.webank.wedatasphere.linkis.protocol.RetryableProtocol

/**
  * Created by enjoyyin on 2018/9/14.
  */
trait RequestTask {
  def getCode: String
  def setCode(code: String): Unit
  def getLock: String
  def setLock(lock: String): Unit
  def getProperties: util.Map[String, Object]
  def setProperties(properties: util.Map[String, Object])
  def data(key: String, value: Object): Unit
}
object RequestTask {
  private val header = "#rt_"
  val RESULT_SET_STORE_PATH = header + "rs_store_path"
}
class RequestTaskExecute extends RequestTask {
  private var code: String = _
  private var lock: String = _
  private var properties: util.Map[String, Object] = new util.HashMap[String, Object]
  override def getCode: String = code

  override def setCode(code: String): Unit = this.code = code

  override def getLock: String = lock

  override def setLock(lock: String): Unit = this.lock = lock

  override def getProperties: util.Map[String, Object] = properties

  override def setProperties(properties: util.Map[String, Object]): Unit = this.properties = properties

  override def data(key: String, value: Object): Unit = {
    if(properties == null) properties = new util.HashMap[String, Object]
    properties.put(key, value)
  }
}
case class RequestTaskPause(execId: String)
case class RequestTaskResume(execId: String)
case class RequestTaskKill(execId: String)
//Instance pass the corresponding instance information, used to print the log, to facilitate troubleshooting
//instance传递对应的实例信息，用于打印日志，方便排查问题
case class RequestEngineLock(instance: String, timeout: Long)
case class RequestEngineUnlock(instance: String, lock: String)

/**
  * The status of requesting job execution, mainly used for:<br>
  * 1. If the engine has not sent information to the entity for a long time, the enumeration periodically requests status information<br>
  * 2. If the entance of the request engine is hanged, the newly taken entity first requests the status of the job from the engine, the engine maintains the new sender information,
  * and then sends log information to the sender.
  * 请求Job执行的状态，主要用于：<br>
  * 1. 如果engine很久没有给entrance发送信息，entrance定时请求一次状态信息<br>
  * 2. 如果请求engine的entrance挂掉了，新接手的entrance第一次向engine请求Job的状态，engine维系新的sender信息，后续向该sender发送日志信息
  */
case class RequestTaskStatus(execId: String)