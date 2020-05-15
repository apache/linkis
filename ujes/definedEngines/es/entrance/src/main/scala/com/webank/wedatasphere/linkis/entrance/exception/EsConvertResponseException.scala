package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 *
 * @author wang_zh
 * @date 2020/5/12
 */
case class EsConvertResponseException(errorMsg: String) extends ErrorException(70113, errorMsg)