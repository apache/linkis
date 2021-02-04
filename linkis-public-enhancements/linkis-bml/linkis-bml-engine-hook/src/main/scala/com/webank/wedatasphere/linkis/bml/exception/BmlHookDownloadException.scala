package com.webank.wedatasphere.linkis.bml.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/9/25
  * Description:
  */
case class BmlHookDownloadException(errMsg:String) extends ErrorException(50046, errMsg)
