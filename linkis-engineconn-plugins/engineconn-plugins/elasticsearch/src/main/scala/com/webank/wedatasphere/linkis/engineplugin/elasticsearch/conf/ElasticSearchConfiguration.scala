package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

object ElasticSearchConfiguration {

  // es client
  val ES_CLUSTER = CommonVars("wds.linkis.es.cluster", "")
  val ES_DATASOURCE_NAME = CommonVars("wds.linkis.datasource", "default_datasource")
  val ES_AUTH_CACHE = CommonVars("wds.linkis.es.auth.cache", false)
  val ES_USERNAME = CommonVars("wds.linkis.es.username", "")
  val ES_PASSWORD = CommonVars("wds.linkis.es.password", "")
  val ES_SNIFFER_ENABLE = CommonVars("wds.linkis.es.sniffer.enable", false)
  val ES_HTTP_METHOD = CommonVars("wds.linkis.es.http.method", "GET")
  val ES_HTTP_ENDPOINT = CommonVars("wds.linkis.es.http.endpoint", "/_search")
  val ES_HTTP_SQL_ENDPOINT = CommonVars("wds.linkis.es.sql.endpoint", "/_sql")
  val ES_SQL_FORMAT = CommonVars("wds.linkis.es.sql.format", "{\"query\": \"%s\"}")
  val ES_HTTP_HEADER_PREFIX = "wds.linkis.es.headers."

  // entrance resource
  val ENTRANCE_MAX_JOB_INSTANCE = CommonVars("wds.linkis.es.max.job.instance", 100)
  val ENTRANCE_PROTECTED_JOB_INSTANCE = CommonVars("wds.linkis.es.protected.job.instance", 20)
  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.es.default.limit", 5000)

  // resultSet
  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("wds.linkis.resultSet.cache.max", new ByteType("512k"))

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.concurrent.limit", 100)

  val DEFAULT_VERSION = CommonVars[String]("wds.linkis.engineconn.io.version", "7.6.2")

}
