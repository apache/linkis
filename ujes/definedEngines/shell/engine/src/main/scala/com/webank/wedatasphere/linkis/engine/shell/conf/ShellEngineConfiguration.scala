package com.webank.wedatasphere.linkis.engine.shell.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
object ShellEngineConfiguration {
  val OUTPUT_LIMIT = CommonVars("bdp.dataworkcloud.shell.output.limit", 5000)
  val WORKING_DIR = CommonVars("bdp.dataworkcloud.shell.working.dir", "/appcom/")
}
