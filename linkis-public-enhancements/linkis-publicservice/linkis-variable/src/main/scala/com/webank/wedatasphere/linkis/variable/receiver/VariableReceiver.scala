/*
 *
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

package com.webank.wedatasphere.linkis.variable.receiver

import com.webank.wedatasphere.linkis.protocol.variable.{RequestQueryAppVariable, RequestQueryGlobalVariable}
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.variable.service.VariableService

import scala.concurrent.duration.Duration

class VariableReceiver extends Receiver{

  private var variableService : VariableService = _

  def this(variableService : VariableService) = {
    this()
    this.variableService = variableService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case RequestQueryGlobalVariable(userName:String)=>variableService.queryGolbalVariable(userName)
    case e:RequestQueryAppVariable =>variableService.queryAppVariable(e.userName,e.creator,e.appName)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
