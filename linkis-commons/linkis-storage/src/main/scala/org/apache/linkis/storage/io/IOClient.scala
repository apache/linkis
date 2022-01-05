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
 
package org.apache.linkis.storage.io

import java.util.UUID
import org.apache.linkis.storage.domain.MethodEntity
import org.apache.linkis.storage.exception.StorageErrorException

/**
  * IOClient is used to execute the proxy as the ujes code execution entry in io and get the return result.
  * IOClient用于在io进行代理作为ujes的代码执行入口，并获取返回结果
  */
trait IOClient {
  def execute(user: String, methodEntity: MethodEntity, params:java.util.Map[String,Any]):String

  def executeWithEngine(user: String, methodEntity: MethodEntity, params:java.util.Map[String,Any]):Array[String]
}

object IOClient{
  var ioClient:IOClient = null

  val SUCCESS = "SUCCESS"
  val FAILED = "FAILED"

  def getIOClient():IOClient = {
    if(ioClient == null) throw new StorageErrorException(52004,"You must register IOClient before you can use proxy mode.(必须先注册IOClient，才能使用代理模式)")
    ioClient
  }

  /**
    * This method is called when ioClient is initialized.
    * ioClient初始化时会调用该方法
    * @param client
    */
  def register(client: IOClient):Unit = {
    this.ioClient = client
    println(ioClient)
  }

  def getFSId():String = {
   UUID.randomUUID().toString
  }
}