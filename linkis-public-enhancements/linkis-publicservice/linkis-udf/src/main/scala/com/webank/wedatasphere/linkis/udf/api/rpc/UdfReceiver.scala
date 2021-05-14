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

package com.webank.wedatasphere.linkis.udf.api.rpc

import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.udf.service.UDFTreeService

import scala.concurrent.duration.Duration

class UdfReceiver extends Receiver{

  private var udfTreeService: UDFTreeService = _

  def this(udfTreeService: UDFTreeService) = {
    this()
    this.udfTreeService = udfTreeService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = {
    message match {
      case RequestUdfTree(userName, treeType, treeId, treeCategory) =>
        val udfTree = udfTreeService.getTreeById(treeId, userName, treeType, treeCategory)
        new ResponseUdfTree(udfTree)
      case _ =>
    }
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}

}
