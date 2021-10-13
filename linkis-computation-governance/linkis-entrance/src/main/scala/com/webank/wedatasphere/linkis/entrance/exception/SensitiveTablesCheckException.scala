package org.apache.linkis.entrance.exception

import org.apache.linkis.common.exception.ErrorException


case class SensitiveTablesCheckException(errorMsg:String) extends ErrorException(50079, errorMsg)

case class DangerousGramsCheckException(errorMsg:String) extends ErrorException(50081, errorMsg)
