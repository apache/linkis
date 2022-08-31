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

package org.apache.linkis.engineplugin.trino.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

import java.lang

object TrinoConfiguration {

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.concurrent.limit", 100)

  val ENTRANCE_MAX_JOB_INSTANCE = CommonVars[Int]("wds.linkis.entrance.max.job.instance", 100)

  val ENTRANCE_PROTECTED_JOB_INSTANCE =
    CommonVars[Int]("wds.linkis.entrance.protected.job.instance", 0)

  val ENTRANCE_RESULTS_MAX_CACHE =
    CommonVars("wds.linkis.trino.resultSet.cache.max", new ByteType("512k"))

  val DEFAULT_LIMIT = CommonVars[Int]("wds.linkis.trino.default.limit", 5000)

  val TRINO_HTTP_CONNECT_TIME_OUT =
    CommonVars[java.lang.Long]("wds.linkis.trino.http.connectTimeout", new lang.Long(60)) // seconds

  val TRINO_HTTP_READ_TIME_OUT =
    CommonVars[java.lang.Long]("wds.linkis.trino.http.readTimeout", new lang.Long(60))

  val TRINO_URL = CommonVars[String]("wds.linkis.trino.url", "http://127.0.0.1:8080")
  val TRINO_USER = CommonVars[String]("wds.linkis.trino.user", null)
  val TRINO_PASSWORD = CommonVars[String]("wds.linkis.trino.password", null)
  val TRINO_PASSWORD_CMD = CommonVars[String]("wds.linkis.trino.passwordCmd", null)
  val TRINO_CATALOG = CommonVars[String]("wds.linkis.trino.catalog", "system")
  val TRINO_SCHEMA = CommonVars[String]("wds.linkis.trino.schema", "")
  val TRINO_SOURCE = CommonVars[String]("wds.linkis.trino.source", "global")

  val TRINO_SSL_INSECURED = CommonVars[Boolean]("wds.linkis.trino.ssl.insecured", false)
  val TRINO_SSL_KEYSTORE = CommonVars[String]("wds.linkis.trino.ssl.keystore", null)
  val TRINO_SSL_KEYSTORE_TYPE = CommonVars[String]("wds.linkis.trino.ssl.keystore.type", null)

  val TRINO_SSL_KEYSTORE_PASSWORD =
    CommonVars[String]("wds.linkis.trino.ssl.keystore.password", null)

  val TRINO_SSL_TRUSTSTORE = CommonVars[String]("wds.linkis.trino.ssl.truststore", null)
  val TRINO_SSL_TRUSTSTORE_TYPE = CommonVars[String]("wds.linkis.trino.ssl.truststore.type", null)

  val TRINO_SSL_TRUSTSTORE_PASSWORD =
    CommonVars[String]("wds.linkis.trino.ssl.truststore.password", null)

  val TRINO_FORBID_GRANT = CommonVars[Boolean]("wds.linkis.trino.forbid.grant", true)

  val TRINO_FORBID_MODIFY_SCHEMA =
    CommonVars[Boolean]("wds.linkis.trino.forbid.modifySchema", true)

}
