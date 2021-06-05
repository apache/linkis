package com.webank.wedatasphere.linkis.engineconn.core.execution

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.execution.EngineConnExecution
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnMode._
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnModeLabel

import scala.collection.convert.decorateAsScala._


trait AbstractEngineConnExecution extends EngineConnExecution with Logging {

  protected def canExecute(engineCreationContext: EngineCreationContext): Boolean = {
    engineCreationContext.getLabels().asScala.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode = toEngineConnMode(engineConnModeLabel.getEngineConnMode)
        getSupportedEngineConnModes.contains(mode)
      case _ => false
    }
  }

  protected def getSupportedEngineConnModes: Array[EngineConnMode]

  override def execute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    if(canExecute(engineCreationContext)) {
      warn(s"${getClass.getName} is enabled, now step into it's execution.")
      doExecution(ExecutorManager.getInstance.getReportExecutor, engineCreationContext, engineConn)
    } else {
      warn(s"${getClass.getName} is disabled, skip it's execution.")
    }
  }

  protected def doExecution(executor: Executor,
                            engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn): Unit

  def returnAfterMeExecuted(engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn): Boolean = {
    if(getReturnEngineConnModes.isEmpty) return false
    engineCreationContext.getLabels().asScala.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode: EngineConnMode = engineConnModeLabel.getEngineConnMode
        getReturnEngineConnModes.contains(mode)
      case _ => false
    }
  }

  protected def getReturnEngineConnModes: Array[EngineConnMode] = Array.empty

}
