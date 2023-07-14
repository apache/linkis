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

package org.apache.linkis.instance.label.client

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.LabelUtils
import org.apache.linkis.protocol.label.{
  InsLabelAttachRequest,
  InsLabelQueryRequest,
  InsLabelQueryResponse,
  InsLabelRefreshRequest,
  InsLabelRemoveRequest,
  LabelInsQueryRequest,
  LabelInsQueryResponse
}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.conf.RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME
import org.apache.linkis.server.BDPJettyServerHelper

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter

class InstanceLabelClient extends Logging {

  val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def refreshLabelsToInstance(insLabelRefreshRequest: InsLabelRefreshRequest): Unit = {
    getSender().send(insLabelRefreshRequest)
  }

  def removeLabelsFromInstance(insLabelRemoveRequest: InsLabelRemoveRequest): Unit = {
    getSender().send(insLabelRemoveRequest)
  }

  def attachLabelsToInstance(insLabelAttachRequest: InsLabelAttachRequest): Unit = {
    getSender().send(insLabelAttachRequest)
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
          resp.getLabelList.asScala.foreach(pair =>
            labelList.add(labelBuilderFactory.createLabel[Label[_]](pair.getKey, pair.getValue))
          )
          labelList
        case o =>
          logger.error(s"Invalid response ${BDPJettyServerHelper.gson
            .toJson(o)} from request : ${BDPJettyServerHelper.gson.toJson(request)}")
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
          if (null == resp.getInsList || resp.getInsList.isEmpty) {
            return new util.ArrayList[ServiceInstance]()
          }
          if (resp.getInsList.size() != 1) {
            logger.warn(
              s"Instance num ${resp.getInsList.size()} with labels ${BDPJettyServerHelper.gson.toJson(labelMap)} is not single one."
            )
          }
          resp.getInsList
        case o =>
          logger.error(s"Invalid resp : ${JsonUtils.jackson
            .writeValueAsString(o)} from request : ${BDPJettyServerHelper.gson.toJson(request)}")
          new util.ArrayList[ServiceInstance]()
      }
    }
  }

}

object InstanceLabelClient {

  val INSTANCE_LABEL_SERVER_NAME =
    CommonVars("wds.linkis.instance.label.server.name", "linkis-ps-publicservice")

  private val instanceLabelClient = new InstanceLabelClient

  def getInstance: InstanceLabelClient = instanceLabelClient
}
