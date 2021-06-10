package com.webank.wedatasphere.linkis.entrance.execute.operation

import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress.DefaultProgressOperation


/**
 * Operation for Scriptis
 *
 */
class ScriptisOperation(orchestratorSession: OrchestratorSession) extends DefaultProgressOperation(orchestratorSession){
  override def getName: String = ScriptisOperation.PROGRESS_NAME
}

object ScriptisOperation{
  val PROGRESS_NAME = "progress-scriptis"
}