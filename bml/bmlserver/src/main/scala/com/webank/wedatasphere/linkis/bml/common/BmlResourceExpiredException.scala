package com.webank.wedatasphere.linkis.bml.common

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/31
  * Description:
  */
case class BmlResourceExpiredException(resourceId:String) extends ErrorException(78531, resourceId + "已经过期,不能下载")
