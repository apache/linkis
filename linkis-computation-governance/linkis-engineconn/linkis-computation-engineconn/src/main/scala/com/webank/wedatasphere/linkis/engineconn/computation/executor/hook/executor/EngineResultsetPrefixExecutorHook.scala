package com.webank.wedatasphere.linkis.engineconn.computation.executor.hook.executor

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.utlis.ComputaionEngineContant.JOB_IN_RUNTIME_MAP_KEY
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper

import java.util


class EngineResultsetPrefixExecutorHook extends ComputationExecutorHook with Logging {

  override def getHookName(): String = "EngineResultsetPrefixExecutorHook"

  override def beforeExecutorExecute(engineExecutionContext: EngineExecutionContext, engineCreationContext: EngineCreationContext, codeBeforeHook: String): String = {
    val propMap = engineExecutionContext.getProperties
    Utils.tryAndError {
      val resultsetIndex: Int = {
        if (propMap.containsKey(GovernanceConstant.RESULTSET_INDEX)) {
          propMap.get(GovernanceConstant.RESULTSET_INDEX).asInstanceOf[Int]
        } else {
          -1
        }
      }
      if (resultsetIndex >= 0) {
        engineExecutionContext.setResultSetNum(resultsetIndex)
        info(s"Set resultset aliasNum to ${resultsetIndex}")
      } else {
        warn(s"No resultsetIndex found in props : ${BDPJettyServerHelper.gson.toJson(propMap)} \nDefault resultIndex is 0.")
      }
    }
    codeBeforeHook
  }
}
