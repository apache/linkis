package com.webank.wedatasphere.linkis.metadata.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars


object MdqConfiguration {
  val DEFAULT_STORED_TYPE = CommonVars("bdp.dataworkcloud.datasource.store.type", "orc")
  val DEFAULT_PARTITION_NAME = CommonVars("bdp.dataworkcloud.datasource.default.par.name", "ds")
}
