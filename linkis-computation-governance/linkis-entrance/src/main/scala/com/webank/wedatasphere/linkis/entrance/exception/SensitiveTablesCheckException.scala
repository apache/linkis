package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


case class SensitiveTablesCheckException(errorMsg:String) extends ErrorException(50079, errorMsg)

case class DangerousGramsCheckException(errorMsg:String) extends ErrorException(50081, errorMsg)
