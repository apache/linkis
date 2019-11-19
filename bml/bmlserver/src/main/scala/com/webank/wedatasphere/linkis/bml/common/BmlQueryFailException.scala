package com.webank.wedatasphere.linkis.bml.common

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/28
  * Description:
  */
case class BmlQueryFailException(errorMsg:String) extends ErrorException(70068, errorMsg)
