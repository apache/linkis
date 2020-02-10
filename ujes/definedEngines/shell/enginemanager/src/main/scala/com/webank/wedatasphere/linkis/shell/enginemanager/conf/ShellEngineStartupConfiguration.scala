package com.webank.wedatasphere.linkis.shell.enginemanager.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
object ShellEngineStartupConfiguration {
  val SHELL_CLIENT_MEMORY = CommonVars("shell.client.memory", new ByteType("2g"), "指定shell引擎客户端的内存大小")
  val SHELL_ENGINE_SPRING_APPLICATION_NAME = CommonVars("bdp.dataworkcloud.engine.application.name", "shellEngine")
}
