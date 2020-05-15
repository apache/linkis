package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 *
 * @author wang_zh
 * @date 2020/5/6
 */
case class EsParamsIllegalException(errorMsg: String) extends ErrorException(70112, errorMsg)
