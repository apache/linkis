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

package org.apache.linkis.engineplugin.impala.conf

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.storage.utils.StorageConfiguration

import java.security.KeyStore

object ImpalaConfiguration {

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("linkis.engineconn.concurrent.limit", 100)

  val DEFAULT_LIMIT = CommonVars[Int]("linkis.impala.default.limit", 5000)

  val IMPALA_USER_ISOLATION_MODE =
    CommonVars[Boolean]("linkis.impala.user.isolation.mode", false)

  val IMPALA_ENGINE_USER =
    CommonVars("linkis.impala.engine.user", StorageConfiguration.HDFS_ROOT_USER.getValue)

  val IMPALA_SERVERS = CommonVars[String]("linkis.impala.servers", "127.0.0.1:21050")
  val IMPALA_MAX_CONNECTIONS = CommonVars[Int]("linkis.impala.maxConnections", 10)

  val IMPALA_SSL_ENABLE = CommonVars[Boolean]("linkis.impala.ssl.enable", false)
  val IMPALA_SSL_KEYSTORE = CommonVars[String]("linkis.impala.ssl.keystore", "")

  val IMPALA_SSL_KEYSTORE_TYPE =
    CommonVars[String]("linkis.impala.ssl.keystore.type", KeyStore.getDefaultType)

  val IMPALA_SSL_KEYSTORE_PASSWORD = CommonVars[String]("linkis.impala.ssl.keystore.password", "")
  val IMPALA_SSL_TRUSTSTORE = CommonVars[String]("linkis.impala.ssl.truststore", "")

  val IMPALA_SSL_TRUSTSTORE_TYPE =
    CommonVars[String]("linkis.impala.ssl.truststore.type", KeyStore.getDefaultType)

  val IMPALA_SSL_TRUSTSTORE_PASSWORD =
    CommonVars[String]("linkis.impala.ssl.truststore.password", "")

  val IMPALA_SASL_ENABLE = CommonVars[Boolean]("linkis.impala.sasl.enable", false)
  val IMPALA_SASL_MECHANISM = CommonVars[String]("linkis.impala.sasl.mechanism", "PLAIN")
  val IMPALA_SASL_AUTHORIZATION_ID = CommonVars[String]("linkis.impala.sasl.authorizationId", "")
  val IMPALA_SASL_PROTOCOL = CommonVars[String]("linkis.impala.sasl.protocol", "LDAP")
  val IMPALA_SASL_PROPERTIES = CommonVars[String]("linkis.impala.sasl.properties", "")
  val IMPALA_SASL_USERNAME = CommonVars("linkis.impala.sasl.username", IMPALA_ENGINE_USER.getValue)
  val IMPALA_SASL_PASSWORD = CommonVars[String]("linkis.impala.sasl.password", "")
  val IMPALA_SASL_PASSWORD_CMD = CommonVars[String]("linkis.impala.sasl.password.cmd", "")

  val IMPALA_HEARTBEAT_SECONDS = CommonVars[Int]("linkis.impala.heartbeat.seconds", 1)
  val IMPALA_QUERY_TIMEOUT_SECONDS = CommonVars[Int]("linkis.impala.query.timeout.seconds", 0)
  val IMPALA_QUERY_BATCH_SIZE = CommonVars[Int]("linkis.impala.query.batchSize", 1000)
  val IMPALA_QUERY_OPTIONS = CommonVars[String]("linkis.impala.query.options", "")
}
