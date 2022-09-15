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

package org.apache.linkis.engineconnplugin.sqoop.context

import org.apache.linkis.common.conf.CommonVars

/**
 * Sqoop Params Configuration
 */
object SqoopParamsConfiguration {

  val SQOOP_PARAM_MODE: CommonVars[String] = CommonVars("sqoop.params.name.mode", "sqoop.mode")

  val SQOOP_PARAM_HOST: CommonVars[String] =
    CommonVars("sqoop.params.name.host", "sqoop.args.host")

  val SQOOP_PARAM_PORT: CommonVars[String] = CommonVars("sqoop.params.name.ip", "sqoop.args.port")

  val SQOOP_PARAM_CONNECT_PARAMS: CommonVars[String] =
    CommonVars("sqoop.params.name.ip", "sqoop.args.params")

  val SQOOP_PARAM_CONNECT: CommonVars[String] =
    CommonVars("sqoop.params.name.connect", "sqoop.args.connect")

  val SQOOP_PARAM_DATA_SOURCE: CommonVars[String] =
    CommonVars("sqoop.params.name.data-source", "sqoop.args.datasource.name")

  val SQOOP_PARAM_PREFIX: CommonVars[String] =
    CommonVars("sqoop.params.name.prefix", "sqoop.args.")

  val SQOOP_PARAM_ENV_PREFIX: CommonVars[String] =
    CommonVars("sqoop.params.name.env.prefix", "sqoop.env.")

}
