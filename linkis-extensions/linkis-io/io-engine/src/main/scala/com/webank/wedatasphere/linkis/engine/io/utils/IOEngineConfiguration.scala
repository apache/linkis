package com.webank.wedatasphere.linkis.engine.io.utils

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
  * Created by johnnwang on 2018/10/31.
  */
object IOEngineConfiguration {
  val IO_FS_MAX = CommonVars("wds.linkis.storage.io.fs.num", 5)
  val IO_FS_CLEAR_TIME = CommonVars("wds.linkis.storage.io.fs.clear.time", 1000L)
  val IO_FS_ID_LIMIT = CommonVars("wds.linkis.storage.io.fs.id.limit",Long.MaxValue/3*2)
}
