package org.apache.linkis.entrance.exception

import org.apache.linkis.common.exception.ErrorException


case class JobHistoryFailedException(errorMsg:String) extends ErrorException(50081, errorMsg)
