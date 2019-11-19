package com.webank.wedatasphere.linkis.bml.common

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/31
  * Description:
  */
case class BmlPermissionDeniedException(errorMsg:String) extends ErrorException(75569, errorMsg)
