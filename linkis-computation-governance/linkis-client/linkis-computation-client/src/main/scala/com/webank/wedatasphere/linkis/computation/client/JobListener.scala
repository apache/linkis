package com.webank.wedatasphere.linkis.computation.client

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait JobListener {

  def onJobSubmitted(job: LinkisJob): Unit

  def onJobRunning(job: LinkisJob): Unit

  def onJobFinished(job: LinkisJob): Unit

  def onJobUnknownError(job: LinkisJob, t: Throwable): Unit

}
