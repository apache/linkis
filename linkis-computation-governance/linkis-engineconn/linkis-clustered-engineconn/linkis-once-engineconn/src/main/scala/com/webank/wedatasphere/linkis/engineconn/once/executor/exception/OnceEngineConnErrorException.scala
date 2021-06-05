package com.webank.wedatasphere.linkis.engineconn.once.executor.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * Created by enjoyyin on 2021/4/25.
  */
class OnceEngineConnErrorException(errCode: Int, desc: String) extends ErrorException(errCode, desc) {

  def this(errCode: Int, desc: String, cause: Throwable) = {
    this(errCode, desc)
    initCause(cause)
  }

}
