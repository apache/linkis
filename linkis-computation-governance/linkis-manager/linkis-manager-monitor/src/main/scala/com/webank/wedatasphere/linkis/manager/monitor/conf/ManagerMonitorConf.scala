package com.webank.wedatasphere.linkis.manager.monitor.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}


object ManagerMonitorConf {

  val NODE_HEARTBEAT_MAX_UPDATE_TIME = CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("5m"))

  val ENGINE_KILL_TIMEOUT = CommonVars("wds.linkis.manager.am.engine.kill.timeout", new TimeType("2m"))

  val EM_KILL_TIMEOUT = CommonVars("wds.linkis.manager.am.em.kill.timeout", new TimeType("2m"))

  val MANAGER_MONITOR_ASYNC_POLL_SIZE = CommonVars("wds.linkis.manager.monitor.async.poll.size", 5)

}
