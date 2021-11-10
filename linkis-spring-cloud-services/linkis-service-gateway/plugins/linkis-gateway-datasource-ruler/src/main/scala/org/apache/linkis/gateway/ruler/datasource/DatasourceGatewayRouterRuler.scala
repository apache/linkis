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
 
package org.apache.linkis.gateway.ruler.datasource

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.ruler.datasource.service.DatasourceMapService
import org.apache.linkis.gateway.ujes.route.EntranceGatewayRouterRuler
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.interceptor.ServiceInstanceUtils
import org.apache.linkis.server.{BDPJettyServerHelper, JMap}
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class DatasourceGatewayRouterRuler extends EntranceGatewayRouterRuler with Logging {

  @Autowired
  var datasourceMapService: DatasourceMapService = _

  override def rule(serviceId: String, gatewayContext: GatewayContext): Unit = if(StringUtils.isNotBlank(gatewayContext.getRequest.getRequestBody)) {
    val datasourceName = getDatasourceName(gatewayContext.getRequest.getRequestBody)
    if (StringUtils.isBlank(datasourceName)) return
    debug(s"datasourceName: $datasourceName")
    datasourceMapService.getInstanceByDatasource(datasourceName) match {
      case i: String if StringUtils.isNotBlank(i) => gatewayContext.getGatewayRoute.getServiceInstance.setInstance(i)
      case _ => {
        val newInstance = ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(serviceId)
          .map(item => (item, datasourceMapService.countByInstance(item.getInstance)))
          .sortBy(_._2).map(_._1.getInstance).headOption match {
            case Some(item) => datasourceMapService.insertDatasourceMap(datasourceName, item, serviceId)
            case None => null
          }
        debug(s"newInstance: $newInstance")
        if (StringUtils.isNotBlank(newInstance)) {
          gatewayContext.getGatewayRoute.getServiceInstance.setInstance(newInstance)
        }
      }
    }
  }

  def getDatasourceName(body: String): String = if(StringUtils.isNotBlank(body)) {
    val requestObject = BDPJettyServerHelper.gson.fromJson(body, classOf[util.Map[String, Any]])
    if (requestObject == null || requestObject.get(TaskConstant.PARAMS) == null) return null
    val paramsObject =  requestObject.get(TaskConstant.PARAMS).asInstanceOf[util.Map[String, Any]]
    var datasourceName: String = null
    val startupMap = TaskUtils.getStartupMap(paramsObject)
    val runtimeMap = TaskUtils.getRuntimeMap(paramsObject)
    val properties = new JMap[String, String]
    startupMap.foreach {case (k, v) => if(v != null) properties.put(k, v.toString)}
    runtimeMap.foreach {case (k, v) => if(v != null) properties.put(k, v.toString)}
    properties.get(DatasourceGatewayRouterRuler.DATASOURCE_NAME_KEY) match {
      case s: String => datasourceName = s
      case _ =>
    }
    datasourceName
  } else {
    null
  }

}

object DatasourceGatewayRouterRuler {

  val DATASOURCE_NAME_KEY = "wds.linkis.datasource"

}