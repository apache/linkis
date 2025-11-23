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

package org.apache.linkis.engineconn.core.engineconn

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.errorcode.LinkisEngineconnCoreErrorCodeSummary.NEED_ENGINE_BEFORE_CALL
import org.apache.linkis.engineconn.core.exception.EngineConnFatalException

trait EngineConnManager {

  def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn

  def getEngineConn: EngineConn

}

object EngineConnManager {
  private val engineConnManager: EngineConnManager = new DefaultEngineConnManager

  def getEngineConnManager: EngineConnManager = engineConnManager
}

class DefaultEngineConnManager extends EngineConnManager {

  private var engineConn: EngineConn = _

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    if (engineConn != null) return engineConn
    this.engineConn = EngineConnObject.getEngineConnPlugin.getEngineConnFactory.createEngineConn(
      engineCreationContext
    )
    this.engineConn
  }

  override def getEngineConn: EngineConn = {
    if (null == this.engineConn) {
      throw new EngineConnFatalException(
        NEED_ENGINE_BEFORE_CALL.getErrorCode,
        NEED_ENGINE_BEFORE_CALL.getErrorDesc
      )
    }
    this.engineConn
  }

}
