package com.webank.wedatasphere.linkis.metadata.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


case class MdqIllegalParamException(errMsg:String) extends ErrorException(57895, errMsg)
