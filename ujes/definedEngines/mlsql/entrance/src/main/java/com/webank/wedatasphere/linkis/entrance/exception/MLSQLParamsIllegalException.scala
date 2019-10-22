package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


case class MLSQLParamsIllegalException(errorMsg: String) extends ErrorException(72012, errorMsg)

case class MLSQLSQLFeatureNotSupportedException(errorMsg: String) extends ErrorException(72013, errorMsg)

case class MLSQLStateMentNotInitialException(errorMsg: String) extends ErrorException(72014, errorMsg)