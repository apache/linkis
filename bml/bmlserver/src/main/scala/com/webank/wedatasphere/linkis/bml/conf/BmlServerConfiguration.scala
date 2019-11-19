package com.webank.wedatasphere.linkis.bml.conf

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * created by cooperyang on 2019/5/21
  * Description:
  */
object BmlServerConfiguration {
  val BML_HDFS_PREFIX = CommonVars("wds.linkis.bml.hdfs.prefix", "/tmp/wds-ide")

  val BML_CLEAN_EXPIRED_TIME:CommonVars[Int] = CommonVars[Int]("wds.linkis.bml.cleanExpired.time", 100)

  val BML_CLEAN_EXPIRED_TIME_TYPE = CommonVars("wds.linkis.bml.clean.time.type", TimeUnit.HOURS)

  val BML_MAX_THREAD_SIZE:CommonVars[Int] = CommonVars[Int]("wds.linkis.server.maxThreadSize", 30)

}
