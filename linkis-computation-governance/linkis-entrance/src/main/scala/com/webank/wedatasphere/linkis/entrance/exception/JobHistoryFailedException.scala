package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


case class JobHistoryFailedException(errorMsg:String) extends ErrorException(50081, errorMsg)
