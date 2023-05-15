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

package org.apache.linkis.cs.client.utils

import org.apache.linkis.common.conf.CommonVars

object ContextClientConf {

  val CONTEXT_CLIENT_AUTH_KEY: CommonVars[String] =
    CommonVars[String]("wds.linkis.context.client.auth.key", "Token-Code")

  val CONTEXT_CLIENT_AUTH_VALUE: CommonVars[String] =
    CommonVars[String]("wds.linkis.context.client.auth.value", "BML-AUTH")

  val URL_PREFIX: CommonVars[String] =
    CommonVars[String](
      "wds.linkis.cs.url.prefix",
      "/api/rest_j/v1/contextservice",
      "The url prefix of the cs service."
    )

  val HEART_BEAT_ENABLED: CommonVars[String] =
    CommonVars[String]("wds.linkis.cs.heartbeat.enabled", "true")

  val CS_CONNECTION_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.cs.connection.timeout", 3 * 60 * 1000)

  val CS_READ_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.cs.read.timeout", 3 * 60 * 1000)

}
