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

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.protocol.label.{InsLabelAttachRequest, InsLabelQueryRequest, InsLabelQueryResponse, InsLabelRemoveRequest, LabelInsQueryRequest, LabelInsQueryResponse}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper

import java.util
import scala.collection.JavaConverters.asScalaBufferConverter


class InstanceLabelClient extends Logging {

  val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def attachLabelsToInstance(insLabelAttachRequest: InsLabelAttachRequest): Unit = {
    getSender().send(insLabelAttachRequest)
  }

  def removeLabelsFromInstance(insLabelRemoveRequest: InsLabelRemoveRequest): Unit = {
    getSender().send(insLabelRemoveRequest)
  }

  def getSender(): Sender = {
    Sender.getSender(InstanceLabelClient.INSTANCE_LABEL_SERVER_NAME.getValue)
  }

  def getLabelFromInstance(serviceInstance: ServiceInstance): util.List[Label[_]] = {
    val request = new InsLabelQueryRequest(serviceInstance)
    Utils.tryAndError {
      getSender().ask(request) match {
        case resp: InsLabelQueryResponse =>
          val labelList = new util.ArrayList[Label[_]]()
          resp.getLabelList.asScala.foreach(pair => labelList.add(labelBuilderFactory.createLabel[Label[_]](pair.getKey, pair.getValue)))
          labelList
        case o =>
          error(s"Invalid response ${BDPJettyServerHelper.gson.toJson(o)} from request : ${BDPJettyServerHelper.gson.toJson(request)}")
          new util.ArrayList[Label[_]]
      }
    }
  }

  def getInstanceFromLabel(labels: util.List[Label[_]]): util.List[ServiceInstance] = {
    Utils.tryAndError {
      val request = new LabelInsQueryRequest()
      val labelMap = LabelUtils.labelsToMap(labels)
      request.setLabels(labelMap.asInstanceOf[util.HashMap[String, Object]])
      Sender.getSender(PUBLIC_SERVICE_APPLICATION_NAME.getValue).ask(request) match {
        case resp: LabelInsQueryResponse =>
          if (resp.getInsList.size() != 1) {
            warn(s"Instance num ${resp.getInsList.size()} with labels ${BDPJettyServerHelper.gson.toJson(labelMap)} is not single one.")
          }
          resp.getInsList
        case o =>
          error(s"Invalid resp : ${BDPJettyServerHelper.gson.toJson(o)} from request : ${BDPJettyServerHelper.gson.toJson(request)}")
          new util.ArrayList[ServiceInstance]()
      }
    }
  }
}

object InstanceLabelClient {
  val INSTANCE_LABEL_SERVER_NAME = CommonVars("wds.linkis.instance.label.server.name", "linkis-ps-publicservice")

  private val instanceLabelClient = new InstanceLabelClient

  def getInstance = instanceLabelClient
}