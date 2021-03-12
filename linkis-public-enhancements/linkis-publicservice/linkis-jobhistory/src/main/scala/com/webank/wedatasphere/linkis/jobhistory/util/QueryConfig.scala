package com.webank.wedatasphere.linkis.jobhistory.util

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object QueryConfig {
  val CACHE_MAX_EXPIRE_HOUR = CommonVars[Long]("wds.linkis.query.cache.max.expire.hour", 1L)
  val CACHE_DAILY_EXPIRE_ENABLED = CommonVars[Boolean]("wds.linkis.query.cache.daily.expire.enabled", true)
  val CACHE_MAX_SIZE = CommonVars[Long]("wds.linkis.query.cache.max.size", 10000L)
  val CACHE_CLEANING_INTERVAL_MINUTE = CommonVars[Int]("wds.linkis.query.cache.cleaning.interval.minute", 30)
}
