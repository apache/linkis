package com.webank.wedatasphere.linkis.engine.sqoop.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
 * @Classname SqoopEngineConfiguration
 * @Description TODO
 * @Date 2020/8/19 17:48
 * @Created by limeng
 */
object SqoopEngineConfiguration {
  val OUTPUT_LIMIT = CommonVars("bdp.dataworkcloud.sqoop.output.limit", 5000)
}
