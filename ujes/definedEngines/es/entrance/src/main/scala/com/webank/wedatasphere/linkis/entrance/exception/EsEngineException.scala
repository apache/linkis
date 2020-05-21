package com.webank.wedatasphere.linkis.entrance.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 *
 * @author wang_zh
 * @date 2020/5/20
 */
case class EsEngineException(errorMsg: String) extends ErrorException(70114, errorMsg)
