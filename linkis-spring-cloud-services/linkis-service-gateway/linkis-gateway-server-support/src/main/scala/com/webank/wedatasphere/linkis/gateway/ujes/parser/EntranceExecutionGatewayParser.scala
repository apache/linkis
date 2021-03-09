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

package com.webank.wedatasphere.linkis.gateway.ujes.parser

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.parser.AbstractGatewayParser
import com.webank.wedatasphere.linkis.gateway.springcloud.SpringCloudGatewayConfiguration._
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
  * created by cooperyang on 2019/5/15.
  */
@Component
class EntranceExecutionGatewayParser extends AbstractGatewayParser {

  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean = gatewayContext.getRequest.getRequestURI match {
    case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(_, _) => true
    case _ => false
  }

  override def parse(gatewayContext: GatewayContext): Unit = gatewayContext.getRequest.getRequestURI match {
    case EntranceExecutionGatewayParser.ENTRANCE_EXECUTION_REGEX(version, _) =>
      if(sendResponseWhenNotMatchVersion(gatewayContext, version)) return
      //var (creator, executeApplicationName): (String, String) = null
      var creator:String = null
      var executeApplicationName:String = null
      if(StringUtils.isNotBlank(gatewayContext.getRequest.getRequestBody)) {
        val json = BDPJettyServerHelper.gson.fromJson(gatewayContext.getRequest.getRequestBody, classOf[java.util.Map[String, Object]])
        json.get(TaskConstant.EXECUTEAPPLICATIONNAME) match {
          case s: String => executeApplicationName = s
          case _ =>
        }
        json.get(TaskConstant.REQUESTAPPLICATIONNAME) match {
          case s: String => creator = s
          case _ =>
        }
      }
      val path = gatewayContext.getRequest.getRequestURI
      if(StringUtils.isBlank(executeApplicationName)) {
        sendErrorResponse(s"requestUri $path need request parameter " + TaskConstant.EXECUTEAPPLICATIONNAME, gatewayContext)
      } else {
        info(s"GatewayParser parse requestUri $path to service $creator or $executeApplicationName.")
        if(StringUtils.isNotBlank(creator))  gatewayContext.getGatewayRoute.getParams.put(TaskConstant.REQUESTAPPLICATIONNAME, creator)
        gatewayContext.getGatewayRoute.setServiceInstance(ServiceInstance(executeApplicationName, null))
      }
    case _ =>
  }
}

object EntranceExecutionGatewayParser {
  val ENTRANCE_HEADER = normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/entrance/"
  val ENTRANCE_EXECUTION_REGEX = (ENTRANCE_HEADER + "(execute|backgroundservice)").r
}