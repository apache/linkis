package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * Description:
  */
case class SensitiveTablesCheckException(errorMsg:String) extends ErrorException(50079, errorMsg)
