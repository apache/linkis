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

package org.apache.linkis.server.utils

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.server.conf.ServerConfiguration

import scala.collection.JavaConverters._

object LinkisMainHelper {

  private val SPRING_STAR = "spring."

  val SERVER_NAME_KEY = "serviceName"

  def formatPropertyFiles(serviceName: String): Unit = {
    sys.props.put("wds.linkis.configuration", "linkis.properties")
    sys.props.put("wds.linkis.server.conf", s"$serviceName.properties")
  }

  def formatPropertyFiles(mainPropertiesName: String, serviceName: String): Unit = {
    sys.props.put("wds.linkis.configuration", s"$mainPropertiesName.properties")
    sys.props.put("wds.linkis.server.conf", s"$serviceName.properties")
  }

  // TODO wait for linkis re-written
  @deprecated
  def addExtraPropertyFiles(filePaths: String*): Unit = {
    sys.props.put("wds.linkis.server.confs", filePaths.mkString(","))
  }

  def getExtraSpringOptions(profilesName: String): Array[String] = {
    val servletPath = ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue
    var resArr =
      s"--spring.profiles.active=$profilesName" +: s"--spring.mvc.servlet.path=$servletPath" +: CommonVars.properties.asScala
        .filter { case (k, v) => k != null && k.startsWith(SPRING_STAR) }
        .map { case (k, v) =>
          val realKey = k.substring(SPRING_STAR.length)
          s"--$realKey=$v"
        }
        .toArray
    if (Configuration.IS_PROMETHEUS_ENABLE.getValue) {
      var prometheusEndpoint = Configuration.PROMETHEUS_ENDPOINT.getValue
      if (ServerConfiguration.IS_GATEWAY.getValue.equals("false")) {
        prometheusEndpoint = servletPath + prometheusEndpoint
      }
      resArr = resArr :+ s"--prometheus.endpoint=$prometheusEndpoint"
    }
    resArr
  }

}
