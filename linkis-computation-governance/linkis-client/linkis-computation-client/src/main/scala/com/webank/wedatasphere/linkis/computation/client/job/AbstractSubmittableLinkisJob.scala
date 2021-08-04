package com.webank.wedatasphere.linkis.computation.client.job

import com.webank.wedatasphere.linkis.computation.client.{JobListener, LinkisJobMetrics}

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait AbstractSubmittableLinkisJob extends SubmittableLinkisJob {

  private var jobMetrics: LinkisJobMetrics = _

  protected def getJobListeners: Array[JobListener]

  override def submit(): Unit = {
    val startTime = System.currentTimeMillis
    doSubmit()
    jobMetrics = new LinkisJobMetrics(getId)
    jobMetrics.setClientSubmitTime(startTime)
    getJobListeners.foreach(_.onJobSubmitted(this))
  }

  protected def doSubmit(): Unit

  override def getJobMetrics: LinkisJobMetrics = jobMetrics

}
