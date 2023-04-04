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

package org.apache.linkis.gateway.parser

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.server.BDPJettyServerHelper

import java.util

import scala.collection.JavaConverters._

trait RouteLabelParser {

  /**
   * Parse main
   * @param gatewayContext
   *   context
   */
  def parse(gatewayContext: GatewayContext): util.List[RouteLabel]
}

class GenericRoueLabelParser extends RouteLabelParser with Logging {

  override def parse(gatewayContext: GatewayContext): util.List[RouteLabel] = {
    val requestBody = Option(gatewayContext.getRequest.getRequestBody)
    requestBody match {
      case Some(body) =>
        val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
        val json = Utils.tryCatch {
          BDPJettyServerHelper.gson.fromJson(body, classOf[java.util.Map[String, Object]])
        } { case e: Exception =>
          logger.warn("Parse error. " + e.getMessage)
          new util.HashMap[String, Object]()
        }
        val labels: util.List[Label[_]] = json.get(TaskConstant.LABELS) match {
          case map: util.Map[String, Object] => labelBuilderFactory.getLabels(map)
          case map: util.Map[String, Any] => labelBuilderFactory.getLabels(map.asInstanceOf)
          case _ => new util.ArrayList[Label[_]]()
        }
        labels.asScala
          .filter(label => label.isInstanceOf[RouteLabel])
          .map(_.asInstanceOf[RouteLabel])
          .asJava
      case _ => null
    }
  }

}
