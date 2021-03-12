package com.webank.wedatasphere.linkis.jobhistory.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object JobhistoryConfiguration {
  val JOB_HISTORY_ADMIN = CommonVars("wds.linkis.jobhistory.admin", "enjoyyin,alexyang,allenlliu,chaogefeng,cooperyang,johnnwang,shanhuang,leeli,neiljianliu,kakacai,neilliao")
  val JOB_HISTORY_SAFE_TRIGGER = CommonVars("wds.linkis.jobhistory.safe.trigger", true).getValue
}
