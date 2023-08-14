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

package org.apache.linkis.engineconnplugin.seatunnel.config

import org.apache.linkis.common.conf.CommonVars

object SeatunnelZetaEnvConfiguration {

  val LINKIS_SEATUNNEL_MASTER: CommonVars[String] =
    CommonVars[String]("linkis.seatunnel.master", "master")

  val LINKIS_SEATUNNEL_VARIABLE: CommonVars[String] =
    CommonVars[String]("linkis.seatunnel.variable", "variable")

  val LINKIS_SEATUNNEL_CONFIG: CommonVars[String] =
    CommonVars[String]("linkis.seatunnel.config", "config")

  val LINKIS_SEATUNNEL_CLUSTER_NAME: CommonVars[String] =
    CommonVars[String]("linkis.seatunnel.cluster", "cluster")

  val GET_LINKIS_SEATUNNEL_MASTER = "--" + LINKIS_SEATUNNEL_MASTER.getValue
  val GET_LINKIS_SEATUNNEL_VARIABLE = "--" + LINKIS_SEATUNNEL_VARIABLE.getValue
  val GET_LINKIS_SEATUNNEL_CONFIG = "--" + LINKIS_SEATUNNEL_CONFIG.getValue
  val GET_LINKIS_SEATUNNEL_CLUSTER_NAME = "--" + LINKIS_SEATUNNEL_CLUSTER_NAME.getValue

}
