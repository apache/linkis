package com.webank.wedatasphere.linkis.enginemanager.exception

import com.webank.wedatasphere.linkis.common.exception.DWCRetryException

/**
  * created by cooperyang on 2019/12/11
  * Description:
  */
class EMRetryException (errCode:Int, desc:String )  extends DWCRetryException(errCode,desc){
}
