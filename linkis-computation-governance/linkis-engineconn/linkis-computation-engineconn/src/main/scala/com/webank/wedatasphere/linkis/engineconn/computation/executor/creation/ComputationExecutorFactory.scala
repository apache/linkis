package com.webank.wedatasphere.linkis.engineconn.computation.executor.creation

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconn.core.creation.AbstractCodeLanguageLabelExecutorFactory
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait ComputationExecutorFactory extends AbstractCodeLanguageLabelExecutorFactory {

  override protected def newExecutor(id: Int,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn,
                                     labels: Array[Label[_]]): ComputationExecutor

}