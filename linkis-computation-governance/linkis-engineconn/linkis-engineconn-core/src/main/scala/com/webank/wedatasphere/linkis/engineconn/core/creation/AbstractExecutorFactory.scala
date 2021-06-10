package com.webank.wedatasphere.linkis.engineconn.core.creation

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.ExecutorFactory


trait AbstractExecutorFactory extends ExecutorFactory {

  protected def newExecutor(id: Int, engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn): Executor

  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn): Executor = {
    val id = ExecutorManager.getInstance.generateExecutorId()
    newExecutor(id, engineCreationContext, engineConn)
  }

}
