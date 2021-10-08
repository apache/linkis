package com.apache.wedatasphere.linkis.governance.common.exception

import com.apache.wedatasphere.linkis.common.exception.LinkisRetryException

class LinkisJobRetryException(desc: String) extends LinkisRetryException(LinkisJobRetryException.JOB_RETRY_ERROR_CODE, desc)

object LinkisJobRetryException {
  val JOB_RETRY_ERROR_CODE = 25000
}