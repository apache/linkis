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

import org.apache.linkis.common.conf.CommonVars

object DSSGatewayConfiguration {
  val DSS_SPRING_NAME: CommonVars[String] = CommonVars("wds.linkis.dss.name", "dss-server")

  val DSS_URL_LABEL_PREFIX: CommonVars[String] =
    CommonVars("wds.dss.gateway.url.prefix.name", "labels")

  val DSS_URL_ROUTE_LABEL_PREFIX: CommonVars[String] =
    CommonVars("wds.dss.gateway.url.prefix.name", "labelsRoute")

  val DSS_URL_APPCONNS: CommonVars[String] = CommonVars("wds.dss.gateway.url.appconns", "visualis")

  val DSS_APPS_SERVER_OTHER_PREFIX: CommonVars[String] =
    CommonVars("wds.dss.gateway.apps.server.other.prefix", "scriptis,apiservice,datapipe,guide")

  val DSS_APPS_SERVER_DISTINCT_NAME: CommonVars[String] =
    CommonVars("wds.dss.gateway.apps.server.distinct.name", "apps")

  val DSS_APPS_SERVER_ISMERGE: CommonVars[Boolean] =
    CommonVars("wds.dss.gateway.apps.server.ismerge", true)

}
