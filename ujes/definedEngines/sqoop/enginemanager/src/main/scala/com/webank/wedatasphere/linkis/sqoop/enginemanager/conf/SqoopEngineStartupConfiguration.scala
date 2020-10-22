package com.webank.wedatasphere.linkis.sqoop.enginemanager.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
 * @Classname SqoopEngineStartupConfiguration
 * @Description TODO
 * @Date 2020/8/24 16:17
 * @Created by limeng
 */
object SqoopEngineStartupConfiguration {
  val SQOOP_CLIENT_MEMORY = CommonVars("sqoop.client.memory", new ByteType("2g"), "指定sqoop引擎客户端的内存大小")
  val SQOOP_ENGINE_SPRING_APPLICATION_NAME = CommonVars("bdp.dataworkcloud.engine.application.name", "sqoopEngine")
}
