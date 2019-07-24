package com.webank.wedatasphere.linkis.engine.io.domain

import com.webank.wedatasphere.linkis.common.io.Fs
import com.webank.wedatasphere.linkis.engine.io.utils.IOEngineConfiguration
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration

/**
  * FS信息记录，包括Entrance的ID信息
  * Created by johnnwang on 2018/10/31.
  */
class FSInfo(val id: Long, val fs: Fs, var lastAccessTime: Long = System.currentTimeMillis()) {
  def timeout = System.currentTimeMillis() - lastAccessTime > (StorageConfiguration.IO_FS_EXPIRE_TIME.getValue + 60 * 1000)
}
