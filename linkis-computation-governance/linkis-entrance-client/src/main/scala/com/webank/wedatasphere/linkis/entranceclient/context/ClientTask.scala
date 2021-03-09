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

package com.webank.wedatasphere.linkis.entranceclient.context

import java.util

import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.server.JMap

/**
  * Created by johnnwang on 2018/10/30.
  */
class ClientTask extends Task {
  private var instance: String = _
  private var execId: String = _
  private var code: String = _

  private var creator: String = _
  private var user: String = _
  private var params: util.Map[String, Any] = new JMap[String, Any](1)
  def setCreator(creator: String): Unit = this.creator = creator
  def getCreator:String = creator
  def setUser(user: String):Unit = this.user = user
  def getUser:String = user
  def setParams(params: util.Map[String, Any]):Unit = this.params = params
  def getParams: util.Map[String, Any] = params

  override def getInstance: String = instance

  override def getExecId: String = execId

  override def setInstance(instance: String): Unit = this.instance = instance

  override def setExecId(execId: String): Unit = this.execId = execId

  def getCode = code

  def setCode(code: String) = this.code = code
}
