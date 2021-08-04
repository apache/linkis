package com.webank.wedatasphere.linkis.computation.client.interactive

import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait ProgressListener {

  def onProgressUpdate(progress: Float, progressInfos: Array[JobProgressInfo]): Unit

}