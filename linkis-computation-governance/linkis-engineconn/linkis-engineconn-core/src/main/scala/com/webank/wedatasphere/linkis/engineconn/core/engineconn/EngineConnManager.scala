/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.core.engineconn

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject

trait EngineConnManager {

  def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn

  def getEngineConn(): EngineConn


}

object EngineConnManager {
  val engineConnManager: EngineConnManager = new DefaultEngineConnManager

  def getEngineConnManager = engineConnManager
}

class DefaultEngineConnManager extends EngineConnManager {

  private var engineConn: EngineConn = _

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    this.engineConn = EngineConnObject.getEngineConnPlugin.getEngineConnFactory.createEngineConn(engineCreationContext)
    this.engineConn
  }

  override def getEngineConn(): EngineConn = this.engineConn

}

