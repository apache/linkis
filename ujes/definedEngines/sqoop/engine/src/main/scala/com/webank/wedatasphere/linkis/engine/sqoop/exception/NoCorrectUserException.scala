package com.webank.wedatasphere.linkis.engine.sqoop.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/17
  * Description:
  */
case class NoCorrectUserException() extends ErrorException(50036, "No illegal user holds this process")


case class SqoopCodeErrorException() extends ErrorException(50037, "shell code is wrong")