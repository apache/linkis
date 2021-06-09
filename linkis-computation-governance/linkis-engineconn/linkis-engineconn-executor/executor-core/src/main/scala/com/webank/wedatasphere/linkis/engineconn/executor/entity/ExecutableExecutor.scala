package com.webank.wedatasphere.linkis.engineconn.executor.entity

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext


trait ExecutableExecutor[R] extends Executor with Logging {

  def execute(engineCreationContext: EngineCreationContext): R

}