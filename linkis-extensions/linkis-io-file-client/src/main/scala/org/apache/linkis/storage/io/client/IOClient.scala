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

package org.apache.linkis.storage.io.client

import org.apache.linkis.manager.label.entity.entrance.{BindEngineLabel, LoadBalanceLabel}
import org.apache.linkis.storage.domain.MethodEntity
import org.apache.linkis.storage.io.utils.IOClientUtils
import org.apache.linkis.storage.utils.StorageConfiguration

/**
 * IOClient is used to execute the proxy as the ujes code execution entry in io and get the return
 * result.
 */
trait IOClient {

  protected val defaultRetry: Int = StorageConfiguration.IO_INIT_RETRY_LIMIT.getValue

  def execute(user: String, methodEntity: MethodEntity, bindEngineLabel: BindEngineLabel): String

  def executeWithRetry(
      user: String,
      methodEntity: MethodEntity,
      bindEngineLabel: BindEngineLabel,
      reTryLimit: Int = defaultRetry
  ): String

}

object IOClient {

  private lazy val clientID = IOClientUtils.generateExecID()

  def getUniqClientID: String = clientID
}
