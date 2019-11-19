package com.webank.wedatasphere.linkis.engine.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException


class MDQErrorException (errCode: Int, desc: String) extends ErrorException(errCode, desc)
