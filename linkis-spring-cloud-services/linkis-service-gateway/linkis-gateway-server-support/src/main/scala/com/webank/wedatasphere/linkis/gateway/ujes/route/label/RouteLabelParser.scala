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

package com.webank.wedatasphere.linkis.gateway.ujes.route.label

import java.util

import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.route.RouteLabel
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper

import scala.collection.JavaConversions._

trait RouteLabelParser {
  /**
   * Parse main
   * @param gatewayContext context
   */
  def parse(gatewayContext: GatewayContext): util.List[RouteLabel]
}

class GenericRoueLabelParser extends RouteLabelParser{

  override def parse(gatewayContext: GatewayContext): util.List[RouteLabel] = {
    val requestBody = Option(gatewayContext.getRequest.getRequestBody)
    requestBody match {
      case Some(body) =>
        val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
        val json = BDPJettyServerHelper.gson.fromJson(body, classOf[java.util.Map[String, Object]])
        val labels: util.List[Label[_]] = json.get(TaskConstant.LABELS) match {
          case map: util.Map[String, Object] => labelBuilderFactory.getLabels(map)
          case map: util.Map[String, Any] => labelBuilderFactory.getLabels(map.asInstanceOf)
          case _ => new util.ArrayList[Label[_]]()
        }
        labels.filter (label => label.isInstanceOf[RouteLabel]).map(_.asInstanceOf[RouteLabel])
      case _ => null
    }
  }
}
