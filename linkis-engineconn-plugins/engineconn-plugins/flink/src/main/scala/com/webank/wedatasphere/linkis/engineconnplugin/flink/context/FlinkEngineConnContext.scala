package com.webank.wedatasphere.linkis.engineconnplugin.flink.context

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.Environment
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext

/**
  * Created by enjoyyin on 2021/4/14.
  */
class FlinkEngineConnContext(environmentContext: EnvironmentContext) {

  private var executionContext: ExecutionContext = _

  def getEnvironmentContext: EnvironmentContext = environmentContext

  def getExecutionContext: ExecutionContext = executionContext

  def setExecutionContext(executionContext: ExecutionContext): Unit = this.executionContext = executionContext

  def newExecutionContextBuilder(environment: Environment): ExecutionContext.Builder =
    ExecutionContext.builder(environmentContext.getDefaultEnv, environment, environmentContext.getDependencies,
      environmentContext.getFlinkConfig)

}
