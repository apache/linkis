package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/12/11
  * Description:
  */
case class SensitiveTablesCheckException(errorMsg:String) extends ErrorException(50079, errorMsg)

case class DangerousGramsCheckException(errorMsg:String) extends ErrorException(50081, errorMsg)
