package com.webank.wedatasphere.linkis.engineconnplugin.datax.context

import com.webank.wedatasphere.linkis.engineconnplugin.datax.client.config.ExecutionContext

class DataxEngineConnContext {
  private var executionContext: ExecutionContext = _

  def getExecutionContext: ExecutionContext = executionContext

  def setExecutionContext(executionContext: ExecutionContext): Unit = this.executionContext = executionContext
}
