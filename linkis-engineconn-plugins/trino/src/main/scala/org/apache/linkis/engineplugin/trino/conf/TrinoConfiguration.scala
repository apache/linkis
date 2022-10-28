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

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.storage.utils.StorageConfiguration

import java.lang

object TrinoConfiguration {

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("linkis.engineconn.concurrent.limit", 100)

  val DEFAULT_LIMIT = CommonVars[Int]("linkis.trino.default.limit", 5000)

  val TRINO_HTTP_CONNECT_TIME_OUT =
    CommonVars[java.lang.Long]("linkis.trino.http.connectTimeout.seconds", new lang.Long(60))

  val TRINO_HTTP_READ_TIME_OUT =
    CommonVars[java.lang.Long]("linkis.trino.http.readTimeout.seconds", new lang.Long(60))

  val TRINO_URL = CommonVars[String]("linkis.trino.url", "http://127.0.0.1:8080")

  val TRINO_PASSWORD = CommonVars[String]("linkis.trino.password", null)
  val TRINO_PASSWORD_CMD = CommonVars[String]("linkis.trino.password.cmd", null)
  val TRINO_CATALOG = CommonVars[String]("linkis.trino.catalog", "system")
  val TRINO_SCHEMA = CommonVars[String]("linkis.trino.schema", "")
  val TRINO_SOURCE = CommonVars[String]("linkis.trino.source", "global")

  val TRINO_SSL_INSECURED = CommonVars[Boolean]("linkis.trino.ssl.insecured", true)
  val TRINO_SSL_KEYSTORE = CommonVars[String]("linkis.trino.ssl.keystore", null)
  val TRINO_SSL_KEYSTORE_TYPE = CommonVars[String]("linkis.trino.ssl.keystore.type", null)

  val TRINO_SSL_KEYSTORE_PASSWORD =
    CommonVars[String]("linkis.trino.ssl.keystore.password", null)

  val TRINO_SSL_TRUSTSTORE = CommonVars[String]("linkis.trino.ssl.truststore", null)
  val TRINO_SSL_TRUSTSTORE_TYPE = CommonVars[String]("linkis.trino.ssl.truststore.type", null)

  val TRINO_SSL_TRUSTSTORE_PASSWORD =
    CommonVars[String]("linkis.trino.ssl.truststore.password", null)

  val TRINO_FORBID_GRANT = CommonVars[Boolean]("linkis.trino.forbid.grant", true)

  val TRINO_FORBID_MODIFY_SCHEMA =
    CommonVars[Boolean]("linkis.trino.forbid.modifySchema", true)

  val TRINO_USER_ISOLATION_MODE =
    CommonVars[Boolean]("linkis.trino.user.isolation.mode", false)

  val TRINO_DEFAULT_USER =
    CommonVars("linkis.trino.default.start.user", StorageConfiguration.HDFS_ROOT_USER.getValue)

  val TRINO_SQL_HOOK_ENABLED = CommonVars("linkis.trino.sql.hook.enabled", true, "trino sql hoook")

}
