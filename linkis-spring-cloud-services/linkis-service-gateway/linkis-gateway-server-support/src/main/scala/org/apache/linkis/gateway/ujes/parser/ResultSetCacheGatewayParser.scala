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

package org.apache.linkis.gateway.ujes.parser

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.parser.AbstractGatewayParser
import org.apache.linkis.gateway.springcloud.SpringCloudGatewayConfiguration.{
  normalPath,
  API_URL_PREFIX
}
import org.apache.linkis.gateway.ujes.parser.ResultSetCacheGatewayParser._
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component

import java.util

import scala.util.matching.Regex

@Component
class ResultSetCacheGatewayParser extends AbstractGatewayParser {

  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean =
    gatewayContext.getRequest.getRequestURI match {
      case RESULT_SET_CACHE_URI_REGEX(_, _) => true
      case _ => false
    }

  private def getServiceInstance(
      gatewayContext: GatewayContext,
      requestBody: util.Map[String, Object]
  ): ServiceInstance = {
    val serviceInstance = get(gatewayContext, requestBody, SERVICE_NAME_KEY)
    if (serviceInstance == null) return null
    val instance = get(gatewayContext, requestBody, INSTANCE_KEY)
    if (instance == null) return null
    ServiceInstance(serviceInstance.asInstanceOf[String], instance.asInstanceOf[String])
  }

  private def get(
      gatewayContext: GatewayContext,
      requestBody: util.Map[String, Object],
      key: String
  ): Any = {
    val value = gatewayContext.getRequest.getQueryParams.get(key)
    if (value != null && value.nonEmpty) value(0)
    else if (requestBody != null && requestBody.containsKey(key)) requestBody.get(key)
    else {
      sendErrorResponse(s"$key does not exist.", gatewayContext)
      null
    }
  }

  override def parse(gatewayContext: GatewayContext): Unit =
    gatewayContext.getRequest.getRequestURI match {
      case RESULT_SET_CACHE_URI_REGEX(version, _) =>
        if (sendResponseWhenNotMatchVersion(gatewayContext, version)) return
        val requestBody =
          if (StringUtils.isEmpty(gatewayContext.getRequest.getRequestBody)) {
            new util.HashMap[String, Object]
          } else {
            BDPJettyServerHelper.jacksonJson.readValue(
              gatewayContext.getRequest.getRequestBody,
              classOf[util.Map[String, Object]]
            )
          }
        val serviceInstance = getServiceInstance(gatewayContext, requestBody)
        if (serviceInstance == null) return
        gatewayContext.getGatewayRoute.setServiceInstance(serviceInstance)
        get(gatewayContext, requestBody, FS_PATH_KEY)
        get(gatewayContext, requestBody, PAGE_SIZE_KEY)
      case _ =>
    }

}

object ResultSetCacheGatewayParser {

  val RESULT_SET_CACHE_URI_REGEX: Regex = (normalPath(
    API_URL_PREFIX
  ) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/engine/resultSetCache[a-zA-Z]*/([a-zA-Z]+)").r

  val SERVICE_NAME_KEY = "serviceName"
  val INSTANCE_KEY = "instance"
  val FS_PATH_KEY = "fsPath"
  val PAGE_SIZE_KEY = "pageSize"
}

@Configuration
class ResultSetCacheGatewayConfiguration extends Logging {

  @Bean
  @ConditionalOnMissingBean
  @Autowired
  def createHttpMessageConvertersObjectFactory(
      converters: HttpMessageConverters
  ): ObjectFactory[HttpMessageConverters] = {
    logger.warn(
      "Notice: no ObjectFactory<HttpMessageConverters> find, ResultSetCache will provide one."
    )
    new ObjectFactory[HttpMessageConverters] {
      override def getObject: HttpMessageConverters = converters
    }
  }

  @Bean
  @ConditionalOnMissingBean
  def createHttpMessageConverters(): HttpMessageConverters = {
    logger.warn("Notice: no HttpMessageConverters find, ResultSetCache will provide one.")
    new HttpMessageConverters(new MappingJackson2HttpMessageConverter)
  }

}
