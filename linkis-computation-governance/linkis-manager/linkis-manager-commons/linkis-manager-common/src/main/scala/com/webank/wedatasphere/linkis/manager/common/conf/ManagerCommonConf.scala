package com.webank.wedatasphere.linkis.manager.common.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars


object ManagerCommonConf {

  val DEFAULT_ENGINE_TYPE = CommonVars("wds.linkis.default.engine.type", "spark")

  val DEFAULT_ENGINE_VERSION = CommonVars("wds.linkis.default.engine.type", "2.4.3")

  val DEFAULT_ADMIN = CommonVars("wds.linkis.manager.admin", "hadoop")

}
