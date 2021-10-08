package com.apache.wedatasphere.linkis.entrance.exception

import com.apache.wedatasphere.linkis.common.exception.ErrorException


case class JobHistoryFailedException(errorMsg:String) extends ErrorException(50081, errorMsg)
