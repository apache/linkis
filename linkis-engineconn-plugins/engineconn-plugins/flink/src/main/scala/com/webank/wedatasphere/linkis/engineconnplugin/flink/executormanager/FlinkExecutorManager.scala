package com.webank.wedatasphere.linkis.engineconnplugin.flink.executormanager

import com.webank.wedatasphere.linkis.engineconn.core.executor.LabelExecutorManagerImpl
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import com.webank.wedatasphere.linkis.engineconn.once.executor.creation.OnceExecutorFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.CodeLanguageLabelExecutorFactory
import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
 * created by cooperyang on 2021/7/7
 * Description:
 */
class FlinkExecutorManager extends LabelExecutorManagerImpl{
  override def getReportExecutor: Executor = if (getExecutors.isEmpty) {
    val labels = defaultFactory match {
      case onceExecutorFactory: OnceExecutorFactory =>
        if (null == engineConn.getEngineCreationContext.getLabels()) Array.empty[Label[_]]
        else engineConn.getEngineCreationContext.getLabels().toArray[Label[_]](Array.empty[Label[_]])
      case labelExecutorFactory: CodeLanguageLabelExecutorFactory =>
        Array[Label[_]](labelExecutorFactory.getDefaultCodeLanguageLabel)
      case _ =>
        if (null == engineConn.getEngineCreationContext.getLabels()) Array.empty[Label[_]]
        else engineConn.getEngineCreationContext.getLabels().toArray[Label[_]](Array.empty[Label[_]])
    }
    createExecutor(engineConn.getEngineCreationContext, labels)
  } else  {
    getExecutors.maxBy {
      case executor: SensibleExecutor => executor.getStatus.ordinal()
      case executor: Executor => executor.getId.hashCode
    }
  }
}
