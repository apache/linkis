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

package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.listener.{ECMEvent, ECMEventListener}
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.ecm.server.listener.{ECMClosedEvent, ECMReadyEvent}
import org.apache.linkis.ecm.server.service.ECMRegisterService
import org.apache.linkis.ecm.server.util.ECMUtils
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.protocol.em.{
  RegisterEMRequest,
  RegisterEMResponse,
  StopEMRequest
}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.SerializableLabel
import org.apache.linkis.rpc.Sender

import java.util
import java.util.Collections

class DefaultECMRegisterService extends ECMRegisterService with ECMEventListener with Logging {

  private var unRegisterFlag = false

  private def readyEvent2RegisterECMRequest(event: ECMReadyEvent): RegisterEMRequest = {
    val request = new RegisterEMRequest
    val instance = Sender.getThisServiceInstance
    request.setUser(Utils.getJvmUser)
    request.setServiceInstance(instance)
    request.setAlias(instance.getApplicationName)
    request.setLabels(getLabelsFromArgs(event.params))
    request.setNodeResource(getEMRegiterResourceFromConfiguration)
    request
  }

  def getLabelsFromArgs(params: Array[String]): util.Map[String, AnyRef] = {
    import scala.collection.JavaConverters._
    val labelRegex = """label\.(.+)\.(.+)=(.+)""".r
    val labels = new util.HashMap[String, AnyRef]()
    // TODO: magic
    labels.asScala += LabelKeyConstant.SERVER_ALIAS_KEY -> Collections.singletonMap(
      "alias",
      ENGINE_CONN_MANAGER_SPRING_NAME
    )

    if (Configuration.IS_MULTIPLE_YARN_CLUSTER.getValue.asInstanceOf[Boolean]) {
      labels.asScala += LabelKeyConstant.YARN_CLUSTER_KEY ->
        (ECM_YARN_CLUSTER_TYPE + "_" + ECM_YARN_CLUSTER_NAME)
    }
    // TODO: group  by key
    labels
  }

  def getEMRegiterResourceFromConfiguration: NodeResource = {
    val maxResource = new LoadInstanceResource(
      ECMUtils.inferDefaultMemory(),
      ECM_MAX_CORES_AVAILABLE,
      ECM_MAX_CREATE_INSTANCES
    )
    val minResource =
      new LoadInstanceResource(ECM_PROTECTED_MEMORY, ECM_PROTECTED_CORES, ECM_PROTECTED_INSTANCES)
    val nodeResource = new CommonNodeResource
    nodeResource.setResourceType(ResourceType.LoadInstance)
    nodeResource.setExpectedResource(Resource.getZeroResource(maxResource))
    nodeResource.setLeftResource(maxResource)
    nodeResource.setLockedResource(Resource.getZeroResource(maxResource))
    nodeResource.setMaxResource(maxResource)
    nodeResource.setMinResource(minResource)
    nodeResource.setUsedResource(Resource.getZeroResource(maxResource))
    nodeResource
  }

  override def onEvent(event: ECMEvent): Unit = event match {
    case event: ECMReadyEvent => registerECM(readyEvent2RegisterECMRequest(event))
    case event: ECMClosedEvent => unRegisterECM(closeEvent2StopECMRequest(event))
    case _ =>
  }

  private def closeEvent2StopECMRequest(event: ECMClosedEvent): StopEMRequest = {
    val request = new StopEMRequest
    val instance = Sender.getThisServiceInstance
    request.setUser(Utils.getJvmUser)
    request.setEm(instance)
    request
  }

  override def registerECM(request: RegisterEMRequest): Unit = Utils.tryCatch {
    logger.info("start register ecm")
    val response = Sender.getSender(MANAGER_SERVICE_NAME).ask(request)
    response match {
      case registerEMResponse: RegisterEMResponse =>
        if (!registerEMResponse.getIsSuccess) {
          logger.error(
            s"Failed to register info to linkis manager, reason: ${registerEMResponse.getMsg}"
          )
          System.exit(1)
        }
      case _ =>
        logger.error(s"Failed to register info to linkis manager, get response is $response")
        System.exit(1)
    }
  } { t =>
    logger.error(s"Failed to register info to linkis manager: ", t)
    System.exit(1)
  }

  override def unRegisterECM(request: StopEMRequest): Unit = {
    logger.info("start unRegister ecm")
    if (!unRegisterFlag) {
      Sender.getSender(MANAGER_SERVICE_NAME).send(request)
    }
    unRegisterFlag = true
  }

}
