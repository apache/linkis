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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.parser.{GenericRoueLabelParser, RouteLabelParser}
import org.apache.linkis.gateway.springcloud.SpringCloudGatewayConfiguration

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.{Bean, Configuration, Scope}

import java.util

@Configuration
@AutoConfigureBefore(Array(classOf[SpringCloudGatewayConfiguration]))
class GatewayRouterConfiguration extends Logging {

  @Bean
  @Scope("prototype")
//   @ConditionalOnMissingBean(Array(classOf[RouteLabelParser]))
  def routeLabelParser(): RouteLabelParser = {
    new GenericRoueLabelParser()
  }

  @Bean
//   @ConditionalOnMissingBean(Array(classOf[AbstractLabelGatewayRouter]))
  def labelGatewayRouter(
      routeLabelParsers: util.List[RouteLabelParser]
  ): AbstractLabelGatewayRouter = {
    logger.info(
      "Use default label gateway router: [" + classOf[DefaultLabelGatewayRouter].getName + "]"
    )
    new DefaultLabelGatewayRouter(routeLabelParsers)
  }

}
