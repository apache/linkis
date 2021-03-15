package com.webank.wedatasphere.linkis.bml.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

case class BmlHookDownloadException(errMsg:String) extends ErrorException(50046, errMsg)
