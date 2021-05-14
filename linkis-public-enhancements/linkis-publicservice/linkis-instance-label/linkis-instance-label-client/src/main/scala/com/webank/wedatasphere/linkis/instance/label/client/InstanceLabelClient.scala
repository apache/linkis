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

package com.webank.wedatasphere.linkis.instance.label.client

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.protocol.label.{InsLabelAttachRequest, InsLabelRemoveRequest}
import com.webank.wedatasphere.linkis.rpc.Sender


class InstanceLabelClient {


  def attachLabelsToInstance(insLabelAttachRequest: InsLabelAttachRequest): Unit = {
    getSender().send(insLabelAttachRequest)
  }

  def getSender(): Sender = {
    Sender.getSender(InstanceLabelClient.INSTANCE_LABEL_SERVER_NAME.getValue)
  }

  def removeLabelsFromInstance(insLabelRemoveRequest: InsLabelRemoveRequest): Unit = {
    getSender().send(insLabelRemoveRequest)
  }
}

object InstanceLabelClient {
  val INSTANCE_LABEL_SERVER_NAME = CommonVars("wds.linkis.instance.label.server.name", "linkis-ps-publicservice")

  private val instanceLabelClient = new InstanceLabelClient

  def getInstance = instanceLabelClient
}