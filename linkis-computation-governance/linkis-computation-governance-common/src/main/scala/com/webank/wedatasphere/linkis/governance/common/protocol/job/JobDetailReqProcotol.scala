package com.webank.wedatasphere.linkis.governance.common.protocol.job

import java.util

import com.webank.wedatasphere.linkis.governance.common.entity.job.{SubJobDetail, SubJobInfo}
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol
import java.util.Date

import scala.beans.BeanProperty


trait JobDetailReq extends RequestProtocol

case class JobDetailReqInsert(jobInfo: SubJobInfo) extends JobDetailReq

case class JobDetailReqUpdate(jobInfo: SubJobInfo) extends JobDetailReq

case class JobDetailReqBatchUpdate(jobInfo: util.ArrayList[SubJobInfo]) extends JobDetailReq

case class JobDetailReqQuery(jobReq: SubJobDetail) extends JobDetailReq

case class JobDetailReqReadAll(jobReq: SubJobDetail) extends JobDetailReq

class RequestOneJobDetail extends JobDetailReq {

  @BeanProperty
  var jobDetail: SubJobDetail = _
  @BeanProperty
  var startTime: Date = _
  @BeanProperty
  var endTime: Date = _
}

case class RequestAllJobDetail(instance: String) extends JobDetailReq

case class ResponseOneJobDetail(jobDetail: SubJobDetail)