package org.apache.linkis.governance.common.exception

import org.apache.linkis.common.exception.LinkisRetryException

class LinkisJobRetryException(desc: String) extends LinkisRetryException(LinkisJobRetryException.JOB_RETRY_ERROR_CODE, desc)

object LinkisJobRetryException {
  val JOB_RETRY_ERROR_CODE = 25000
}