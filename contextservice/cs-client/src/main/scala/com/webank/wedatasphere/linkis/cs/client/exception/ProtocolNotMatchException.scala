package com.webank.wedatasphere.linkis.cs.client.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 * created by cooperyang on 2020/2/18
 * Description:
 */
case class ProtocolNotMatchException(errMsg:String) extends ErrorException(70059, errMsg)
