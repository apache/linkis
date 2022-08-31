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

package org.apache.linkis.engineplugin.elasticsearch.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object ElasticSearchConfiguration {

  // es client
  val ES_CLUSTER = CommonVars("linkis.es.cluster", "127.0.0.1:9200")
  val ES_DATASOURCE_NAME = CommonVars("linkis.es.datasource", "default_datasource")
  val ES_AUTH_CACHE = CommonVars("linkis.es.auth.cache", false)
  val ES_USERNAME = CommonVars("linkis.es.username", "")
  val ES_PASSWORD = CommonVars("linkis.es.password", "")
  val ES_SNIFFER_ENABLE = CommonVars("linkis.es.sniffer.enable", false)
  val ES_HTTP_METHOD = CommonVars("linkis.es.http.method", "GET")
  val ES_HTTP_ENDPOINT = CommonVars("linkis.es.http.endpoint", "/_search")
  val ES_HTTP_SQL_ENDPOINT = CommonVars("linkis.es.sql.endpoint", "/_sql")
  val ES_SQL_FORMAT = CommonVars("linkis.es.sql.format", "{\"query\": \"%s\"}")
  val ES_HTTP_HEADER_PREFIX = "linkis.es.headers."

  // entrance resource
  val ENTRANCE_MAX_JOB_INSTANCE = CommonVars("linkis.es.max.job.instance", 100)
  val ENTRANCE_PROTECTED_JOB_INSTANCE = CommonVars("linkis.es.protected.job.instance", 20)
  val ENGINE_DEFAULT_LIMIT = CommonVars("linkis.es.default.limit", 5000)

  // resultSet
  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("linkis.resultSet.cache.max", new ByteType("512k"))

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("linkis.engineconn.concurrent.limit", 100)

  val DEFAULT_VERSION = CommonVars[String]("linkis.engineconn.io.version", "7.6.2")

}
