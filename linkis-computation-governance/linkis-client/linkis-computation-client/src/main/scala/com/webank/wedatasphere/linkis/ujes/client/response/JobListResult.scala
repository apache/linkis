package com.webank.wedatasphere.linkis.ujes.client.response

import java.util

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty


@DWSHttpMessageResult("/api/rest_j/v\\d+/jobhistory/list")
class JobListResult extends DWSResult {

  @BeanProperty
  var tasks: util.ArrayList[util.Map[String, Object]] = _
  @BeanProperty
  var totalPage: Int = _

}
