package org.apache.linkis.gateway.authentication.conf

import org.apache.linkis.common.conf.CommonVars

/**
  * Created by shangda on 2021/9/8.
  */
object TokenConfiguration {
  val TOKEN_WILD_CHAR: String = "*"
  /**
    * -1 for last forever
    */
  val TOKEN_ELAPSE_TIME_DEFAULT: Int = CommonVars[Int]("wds.linkis.token.elapse.time", -1).getValue

  /**
    * Token Cache
    */
  val TOKEN_CACHE_MAX_SIZE: Int = CommonVars[Int]("wds.linkis.token.cache.max.size", 5000).getValue
  val TOKEN_CACHE_EXPIRE_HOURS: Int = CommonVars[Int]("wds.linkis.token.cache.expire.hours", 12).getValue

}
