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

object SeatunnelSparkEnvConfiguration {

  val LINKIS_SPARK_MASTER: CommonVars[String] =
    CommonVars[String]("linkis.spark.master", "master")

  val LINKIS_SPARK_DEPLOY_MODE: CommonVars[String] =
    CommonVars[String]("linkis.spark.deploy.mode", "deploy-mode")

  val LINKIS_SPARK_CONFIG: CommonVars[String] =
    CommonVars[String]("linkis.spark.config", "config")

  val GET_LINKIS_SPARK_MASTER = "--" + LINKIS_SPARK_MASTER.getValue
  val GET_LINKIS_SPARK_DEPLOY_MODE = "--" + LINKIS_SPARK_DEPLOY_MODE.getValue
  val GET_LINKIS_SPARK_CONFIG = "--" + LINKIS_SPARK_CONFIG.getValue

}
