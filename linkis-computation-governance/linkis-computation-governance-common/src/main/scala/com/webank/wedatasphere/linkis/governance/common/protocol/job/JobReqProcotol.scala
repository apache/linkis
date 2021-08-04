package com.webank.wedatasphere.linkis.governance.common.protocol.job

import java.util

import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol
import java.util.Date

import scala.beans.BeanProperty


trait JobReq extends RequestProtocol

case class JobReqInsert(jobReq: JobRequest) extends JobReq

case class JobReqUpdate(jobReq: JobRequest) extends JobReq

case class JobReqBatchUpdate(jobReq: util.ArrayList[JobRequest]) extends JobReq

case class JobReqQuery(jobReq: JobRequest) extends JobReq

case class JobReqReadAll(jobReq: JobRequest) extends JobReq

class RequestOneJob extends JobReq {

  @BeanProperty
  var jobReq: JobRequest = _
  @BeanProperty
  var startTime: Date = _
  @BeanProperty
  var endTime: Date = _
}

case class RequestAllJob(instance: String) extends JobReq

