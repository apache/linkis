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

package org.apache.linkis.storage.io

import org.apache.linkis.storage.domain.MethodEntity
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.MUST_REGISTER_TOC
import org.apache.linkis.storage.exception.StorageErrorException

import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}

/**
 * IOClient is used to execute the proxy as the ujes code execution entry in io and get the return
 * result. IOClient用于在io进行代理作为ujes的代码执行入口，并获取返回结果
 */
trait IOClient {

  def execute(user: String, methodEntity: MethodEntity, params: java.util.Map[String, Any]): String

  def executeWithEngine(
      user: String,
      methodEntity: MethodEntity,
      params: java.util.Map[String, Any]
  ): Array[String]

}

object IOClient {
  val logger: Logger = LoggerFactory.getLogger(classOf[IOClient])
  var ioClient: IOClient = null

  val SUCCESS = "SUCCESS"
  val FAILED = "FAILED"

  def getIOClient(): IOClient = {
    if (ioClient == null) {
      throw new StorageErrorException(
        MUST_REGISTER_TOC.getErrorCode,
        MUST_REGISTER_TOC.getErrorDesc
      )
    }
    ioClient
  }

  /**
   * This method is called when ioClient is initialized. ioClient初始化时会调用该方法
   * @param client
   *   IOClient
   */
  def register(client: IOClient): Unit = {
    this.ioClient = client
    logger.debug(s"IOClient: ${ioClient.toString} registered")
  }

  def getFSId(): String = {
    UUID.randomUUID().toString
  }

}
