package com.webank.wedatasphere.linkis.governance.common.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


class GovernanceErrorException(errorCode: Int, errorMsg: String)
  extends ErrorException(errorCode, errorMsg) {

  def this(errorCode: Int, errorMsg: String, cause: Throwable) = {
    this(errorCode, errorMsg)
    initCause(cause)
  }

}
