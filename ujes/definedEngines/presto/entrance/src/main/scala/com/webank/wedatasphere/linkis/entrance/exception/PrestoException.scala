package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 * Created by yogafire on 2020/5/14
 */
case class PrestoStateInvalidException(message: String) extends ErrorException(60011, message: String)
