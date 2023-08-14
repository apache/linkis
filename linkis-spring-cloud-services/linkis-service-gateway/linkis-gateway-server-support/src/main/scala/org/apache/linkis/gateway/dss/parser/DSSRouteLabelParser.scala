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

package org.apache.linkis.gateway.dss.parser

import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.parser.RouteLabelParser
import org.apache.linkis.manager.label.entity.route.RouteLabel

import org.springframework.stereotype.Component

import java.util

@Component
class DSSRouteLabelParser extends RouteLabelParser {

  override def parse(gatewayContext: GatewayContext): util.List[RouteLabel] = {
    val routeLabelList = new util.ArrayList[RouteLabel]()
    var requestLabels = gatewayContext.getRequest.getQueryParams
      .getOrDefault(DSSGatewayConfiguration.DSS_URL_LABEL_PREFIX.getValue, null)
    if (requestLabels == null) {
      requestLabels = gatewayContext.getRequest.getQueryParams
        .getOrDefault(DSSGatewayConfiguration.DSS_URL_ROUTE_LABEL_PREFIX.getValue, null)
    }
    if (null != requestLabels && requestLabels.size > 0) {
      val labelNameList = requestLabels(0).replace(" ", "").split(",").toList
      if (labelNameList.size > 0) labelNameList.foreach(labelName => {
        val routeLabel = new RouteLabel
        routeLabel.setRoutePath(labelName)
      })
    }
    if (routeLabelList.isEmpty) {
      val requestBody = Option(gatewayContext.getRequest.getRequestBody)
      requestBody match {
        case Some(body) =>
          if (body.contains("form-data")) {} else {}
        case _ => null
      }
    }

    routeLabelList
  }

}
