package com.apache.wedatasphere.linkis.entrance.exception

import com.apache.wedatasphere.linkis.common.exception.ErrorException


case class SensitiveTablesCheckException(errorMsg:String) extends ErrorException(50079, errorMsg)

case class DangerousGramsCheckException(errorMsg:String) extends ErrorException(50081, errorMsg)
