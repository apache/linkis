package com.webank.wedatasphere.linkis.engineplugin.presto.conf

import java.lang

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

object PrestoConfiguration {

  val ENGINE_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.concurrent.limit", 100)

  val ENTRANCE_MAX_JOB_INSTANCE = CommonVars[Int]("wds.linkis.entrance.max.job.instance", 100)
  val ENTRANCE_PROTECTED_JOB_INSTANCE = CommonVars[Int]("wds.linkis.entrance.protected.job.instance", 0)
  val ENTRANCE_RESULTS_MAX_CACHE = CommonVars("wds.linkis.presto.resultSet.cache.max", new ByteType("512k"))

  val PRESTO_HTTP_CONNECT_TIME_OUT = CommonVars[java.lang.Long]("wds.linkis.presto.http.connectTimeout", new lang.Long(60)) //单位秒
  val PRESTO_HTTP_READ_TIME_OUT = CommonVars[java.lang.Long]("wds.linkis.presto.http.readTimeout", new lang.Long(60))


  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.presto.default.limit", 5000)
  val PRESTO_URL = CommonVars("wds.linkis.presto.url", "http://127.0.0.1:8080")
  val PRESTO_RESOURCE_CONFIG_PATH = CommonVars("wds.linkis.presto.resource.config", "");
  val PRESTO_USER_NAME = CommonVars("wds.linkis.presto.username", "default")
  val PRESTO_PASSWORD = CommonVars("wds.linkis.presto.password", "")
  val PRESTO_CATALOG = CommonVars("wds.linkis.presto.catalog", "system")
  val PRESTO_SCHEMA = CommonVars("wds.linkis.presto.schema", "")
  val PRESTO_SOURCE = CommonVars("wds.linkis.presto.source", "global")
  val PRESTO_REQUEST_MEMORY = CommonVars("presto.session.query_max_total_memory", "8GB")

}
