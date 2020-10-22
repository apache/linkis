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
  val SQOOP_SHELL = CommonVars[String]("wds.linkis.server.sqoop.shell", "sqoop","sqoop shell")
  val WORKING_DIR = CommonVars("bdp.dataworkcloud.shell.working.dir", "/appcom/")
}
