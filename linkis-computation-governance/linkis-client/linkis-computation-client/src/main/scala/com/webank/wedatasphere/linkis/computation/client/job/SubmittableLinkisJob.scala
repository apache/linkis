package com.webank.wedatasphere.linkis.computation.client.job

import com.webank.wedatasphere.linkis.computation.client.LinkisJob

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait SubmittableLinkisJob extends LinkisJob {

  def submit(): Unit

}