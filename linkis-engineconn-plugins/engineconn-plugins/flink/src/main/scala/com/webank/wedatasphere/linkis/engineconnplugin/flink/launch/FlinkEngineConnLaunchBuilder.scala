package com.webank.wedatasphere.linkis.engineconnplugin.flink.launch

import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder

/**
  * Created by enjoyyin on 2021/4/7.
  */
class FlinkEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getCommands(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] = {
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    properties.put(EnvConfiguration.ENGINE_CONN_MEMORY.key, FlinkResourceConfiguration.LINKIS_FLINK_CLIENT_MEMORY.getValue(properties) + "G")
    super.getCommands
  }

  override protected def getNecessaryEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] =
    Array(FLINK_HOME_ENV, FLINK_CONF_DIR_ENV) ++: super.getNecessaryEnvironment

  override protected def ifAddHiveConfigPath: Boolean = true

}
