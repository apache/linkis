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

package org.apache.linkis.configuration.constant

import org.apache.linkis.common.conf.CommonVars

object Constants {

  val LINKIS_API_VERSION: CommonVars[String] =
    CommonVars[String]("linkis.configuration.linkisclient.api.version", "v1")

  val AUTH_TOKEN_KEY: CommonVars[String] =
    CommonVars[String]("linkis.configuration.linkisclient.auth.token.key", "Validation-Code")

  val AUTH_TOKEN_VALUE: CommonVars[String] =
    CommonVars[String]("linkis.configuration.linkisclient.auth.token.value", "BML-AUTH")

  val CONNECTION_MAX_SIZE: CommonVars[Int] =
    CommonVars[Int]("linkis.configuration.linkisclient.connection.max.size", 10)

  val CONNECTION_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("linkis.configuration.linkisclient.connection.timeout", 5 * 60 * 1000)

  val CONNECTION_READ_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("linkis.configuration.linkisclient.connection.read.timeout", 10 * 60 * 1000)

  val AUTH_TOKEN_KEY_SHORT_NAME = "tokenKey"
  val AUTH_TOKEN_VALUE_SHORT_NAME = "tokenValue"
  val CONNECTION_MAX_SIZE_SHORT_NAME = "maxConnection"
  val CONNECTION_TIMEOUT_SHORT_NAME = "connectTimeout"
  val CONNECTION_READ_TIMEOUT_SHORT_NAME = "readTimeout"
  val CLIENT_NAME_SHORT_NAME = "clientName"

}
