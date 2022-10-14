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

package org.apache.linkis.gateway.ujes.route

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.cs.common.entity.source.{ContextID, ContextIDParser}
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.route.AbstractGatewayRouter
import org.apache.linkis.gateway.springcloud.SpringCloudGatewayConfiguration.{
  normalPath,
  API_URL_PREFIX
}
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.interceptor.ServiceInstanceUtils

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util

import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.matching.Regex

/**
 * Description:
 */
@Component
class HaContextGatewayRouter extends AbstractGatewayRouter {

  @Autowired
  private var contextIDParser: ContextIDParser = _

  private val serializationHelper = ContextSerializationHelper.getInstance()

  override def route(gatewayContext: GatewayContext): ServiceInstance = {

    if (
        gatewayContext.getGatewayRoute.getRequestURI.contains(
          RPCConfiguration.CONTEXT_SERVICE_REQUEST_PREFIX
        )
    ) {
      val params: util.HashMap[String, String] = gatewayContext.getGatewayRoute.getParams
      if (!gatewayContext.getRequest.getQueryParams.isEmpty) {
        for ((k, vArr) <- gatewayContext.getRequest.getQueryParams.asScala) {
          if (vArr.nonEmpty) {
            params.putIfAbsent(k, vArr.head)
          }
        }
      }
      if (gatewayContext.getRequest.getHeaders.containsKey(ContextHTTPConstant.CONTEXT_ID_STR)) {
        params.putIfAbsent(
          ContextHTTPConstant.CONTEXT_ID_STR,
          gatewayContext.getRequest.getHeaders.get(ContextHTTPConstant.CONTEXT_ID_STR)(0)
        )
      }
      if (null == params || params.isEmpty) {
        dealContextCreate(gatewayContext)
      } else {
        var contextId: String = null
        for ((key, value) <- params.asScala) {
          if (key.equalsIgnoreCase(ContextHTTPConstant.CONTEXT_ID_STR)) {
            contextId = value
          }
        }
        if (StringUtils.isNotBlank(contextId)) {
          dealContextAccess(contextId, gatewayContext)
        } else {
          dealContextCreate(gatewayContext)
        }
      }
    } else {
      null
    }
  }

  def dealContextCreate(gatewayContext: GatewayContext): ServiceInstance = {
    val serviceId = findService(
      RPCConfiguration.CONTEXT_SERVICE_NAME,
      list => {
        val services = list.filter(_.contains(RPCConfiguration.CONTEXT_SERVICE_NAME))
        services.headOption
      }
    )
    val serviceInstances =
      ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(serviceId.orNull)
    if (serviceInstances.size > 0) {
      val index = new Random().nextInt(serviceInstances.size)
      serviceInstances(index)
    } else {
      logger.error(s"No valid instance for service : " + serviceId.orNull)
      null
    }
  }

  def dealContextAccess(contextIdStr: String, gatewayContext: GatewayContext): ServiceInstance = {
    val contextId: String = {
      var tmpId: String = null
      if (serializationHelper.accepts(contextIdStr)) {
        val contextID: ContextID =
          serializationHelper.deserialize(contextIdStr).asInstanceOf[ContextID]
        if (null != contextID) {
          tmpId = contextID.getContextId
        } else {
          logger.error(s"Deserializate contextID null. contextIDStr : " + contextIdStr)
        }
      } else {
        logger.error(s"ContxtIDStr cannot be deserialized. contextIDStr : " + contextIdStr)
      }
      if (null == tmpId) {
        contextIdStr
      } else {
        tmpId
      }
    }
    val instances = contextIDParser.parse(contextId)
    var serviceId: Option[String] = None
    serviceId = findService(
      RPCConfiguration.CONTEXT_SERVICE_NAME,
      list => {
        val services = list.filter(_.contains(RPCConfiguration.CONTEXT_SERVICE_NAME))
        services.headOption
      }
    )
    val serviceInstances =
      ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(serviceId.orNull)
    if (instances.size() > 0) {
      serviceId.map(ServiceInstance(_, instances.get(0))).orNull
    } else if (serviceInstances.size > 0) {
      serviceInstances(0)
    } else {
      logger.error(s"No valid instance for service : " + serviceId.orNull)
      null
    }
  }

}

object HaContextGatewayRouter {

  val CONTEXT_ID_STR: String = "contextId"

  @deprecated("please use RPCConfiguration.CONTEXT_SERVICE_REQUEST_PREFIX")
  val CONTEXT_SERVICE_REQUEST_PREFIX = RPCConfiguration.CONTEXT_SERVICE_REQUEST_PREFIX

  @deprecated("please use RPCConfiguration.CONTEXT_SERVICE_NAME")
  val CONTEXT_SERVICE_NAME: String =
    if (
        RPCConfiguration.ENABLE_PUBLIC_SERVICE.getValue && RPCConfiguration.PUBLIC_SERVICE_LIST
          .contains(RPCConfiguration.CONTEXT_SERVICE_REQUEST_PREFIX)
    ) {
      RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME.getValue
    } else {
      RPCConfiguration.CONTEXT_SERVICE_APPLICATION_NAME.getValue
    }

  val CONTEXT_REGEX: Regex =
    (normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/contextservice/" + ".+").r

}
