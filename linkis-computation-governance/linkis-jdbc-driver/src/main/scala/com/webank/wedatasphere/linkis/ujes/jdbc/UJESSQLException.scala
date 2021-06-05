package com.webank.wedatasphere.linkis.ujes.jdbc

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * Created by owenxu on 2019/8/8
  */
class UJESSQLException (errorCode: UJESSQLErrorCode) extends ErrorException(errorCode.getCode,errorCode.getMsg) {
  def this(errorCode: UJESSQLErrorCode, msg: String) {
    this(errorCode)
    setErrCode(errorCode.getCode)
    setDesc(msg)
  }


  /**
    * add to deal with errorinfo derived from jobInfo
    * @param errorCode
    * @param msg
    */
  def this(errorCode: Int,msg: String) {
    this(UJESSQLErrorCode.ERRORINFO_FROM_JOBINFO)
    setDesc(msg)
    setErrCode(errorCode)
  }

}
