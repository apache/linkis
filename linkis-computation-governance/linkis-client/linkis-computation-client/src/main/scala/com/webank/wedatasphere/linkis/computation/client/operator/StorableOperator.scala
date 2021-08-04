package com.webank.wedatasphere.linkis.computation.client.operator

import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.response.JobSubmitResult

/**
  * Created by enjoyyin on 2021/6/6.
  */
trait StorableOperator[T] extends Operator[T] {

  private var jobSubmitResult: JobSubmitResult = _
  private var ujesClient: UJESClient = _

  protected def getJobSubmitResult: JobSubmitResult = jobSubmitResult
  protected def getUJESClient: UJESClient = ujesClient

  def setJobSubmitResult(jobSubmitResult: JobSubmitResult): this.type = {
    this.jobSubmitResult = jobSubmitResult
    this
  }

  def setUJESClient(ujesClient: UJESClient): this.type = {
    this.ujesClient = ujesClient
    this
  }

}
