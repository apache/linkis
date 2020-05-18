package com.webank.wedatasphere.linkis.entrance.configuration

import java.lang

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
 * Created by yogafire on 2020/4/30
 */
object PrestoConfiguration {

  val PRESTO_HTTP_CONNECT_TIME_OUT = CommonVars[java.lang.Long]("wds.linkis.presto.http.connectTimeout", new lang.Long(60)) //单位秒
  val PRESTO_HTTP_READ_TIME_OUT = CommonVars[java.lang.Long]("wds.linkis.presto.http.readTimeout", new lang.Long(60))


  val PRESTO_DEFAULT_LIMIT = CommonVars("wds.linkis.presto.default.limit", 5000)
  val PRESTO_URL = CommonVars("wds.linkis.presto.url", "http://127.0.0.1:8080")
  val PRESTO_USER_NAME = CommonVars("wds.linkis.presto.username", "default")
  val PRESTO_PASSWORD = CommonVars("wds.linkis.presto.password", "")
  val PRESTO_CATALOG = CommonVars("wds.linkis.presto.catalog", "system")
  val PRESTO_SCHEMA = CommonVars("wds.linkis.presto.schema", "")
  val PRESTO_RESOURCE = CommonVars("wds.linkis.presto.source", "global")
  val PRESTO_RESULTS_MAX_CACHE = CommonVars("wds.linkis.presto.resultSet.cache.max", new ByteType("512k"))


  val PRESTO_ENGINE_REQUEST_MEMORY = CommonVars[Int]("query_max_total_memory", 4) //单位GB
  val PRESTO_ENGINE_QUEUE_NAME = CommonVars[String]("resource_group", "global")
}
