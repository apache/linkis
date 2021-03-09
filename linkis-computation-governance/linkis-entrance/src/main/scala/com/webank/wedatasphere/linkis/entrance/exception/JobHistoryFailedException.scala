package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 * created by cooperyang on 2020/1/2
 * Description:
 */
case class JobHistoryFailedException(errorMsg:String) extends ErrorException(50081, errorMsg)
