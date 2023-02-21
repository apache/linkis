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

package org.apache.linkis.engineconn.common.hook

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn

import org.apache.commons.lang3.StringUtils

trait EngineConnHook {

  def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit

  def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit

  def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit

  def afterEngineServerStartFailed(
      engineCreationContext: EngineCreationContext,
      throwable: Throwable
  ): Unit = {}

  def afterEngineServerStartSuccess(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {}

}

object EngineConnHook extends Logging {

  private var engineConnHooks: Array[EngineConnHook] = _

  private def initEngineConnHooks(isOnceMode: Boolean): Unit = {

    val hooks = if (isOnceMode) {
      EngineConnConf.ENGINE_CONN_ONCE_HOOKS.getValue
    } else {
      EngineConnConf.ENGINE_CONN_HOOKS.getValue
    }
    if (StringUtils.isNotBlank(hooks)) {
      engineConnHooks = hooks
        .split(",")
        .map(_.trim)
        .filter(StringUtils.isNotBlank)
        .map(Utils.tryAndWarn(Utils.getClassInstance[EngineConnHook](_)))
        .filter(_ != null)
    } else {
      engineConnHooks = Array.empty
    }

  }

  def getEngineConnHooks(isOnceMode: Boolean = false): Array[EngineConnHook] = {
    if (engineConnHooks == null) {
      initEngineConnHooks(isOnceMode)
    }
    engineConnHooks
  }

}
