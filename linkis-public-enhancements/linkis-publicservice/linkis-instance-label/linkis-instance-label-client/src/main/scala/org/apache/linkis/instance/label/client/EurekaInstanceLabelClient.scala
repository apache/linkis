/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.instance.label.client

import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.protocol.label.{InsLabelRefreshRequest, InsLabelRemoveRequest}
import org.apache.linkis.rpc.Sender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.serviceregistry.Registration
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.stereotype.Component

import java.util
import javax.annotation.PostConstruct


@Component
class EurekaInstanceLabelClient extends Logging {


  @Autowired
  private var registration: Registration = _



  @PostConstruct
  def init(): Unit = {
    info("EurekaInstanceLabelClient init")
    val metadata = registration.getMetadata
    if (null != metadata && metadata.containsKey(LabelKeyConstant.ROUTE_KEY) && StringUtils.isNoneBlank(metadata.get(LabelKeyConstant.ROUTE_KEY))) {
      info(s"Start to register label for instance $metadata")
      val labels = new util.HashMap[String, Object]()
      labels.put(LabelKeyConstant.ROUTE_KEY, metadata.get(LabelKeyConstant.ROUTE_KEY))
      val insLabelRefreshRequest = new InsLabelRefreshRequest
      insLabelRefreshRequest.setLabels(metadata.asInstanceOf[java.util.Map[String, Object]])
      insLabelRefreshRequest.setServiceInstance(Sender.getThisServiceInstance)
      InstanceLabelClient.getInstance.refreshLabelsToInstance(insLabelRefreshRequest)
    }
  }

  @EventListener(classes = Array(classOf[ContextClosedEvent]))
  def shutdown(contextClosedEvent: ContextClosedEvent): Unit = {
    info("To remove labels for instance")
    val insLabelRemoveRequest = new InsLabelRemoveRequest
    insLabelRemoveRequest.setServiceInstance(Sender.getThisServiceInstance)
    InstanceLabelClient.getInstance.removeLabelsFromInstance(insLabelRemoveRequest)
    info("success to send clear label rpc request")
  }


}
