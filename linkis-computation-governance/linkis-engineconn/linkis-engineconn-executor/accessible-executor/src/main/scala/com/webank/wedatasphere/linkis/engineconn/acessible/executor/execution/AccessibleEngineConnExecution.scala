package com.webank.wedatasphere.linkis.engineconn.acessible.executor.execution

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.execution.EngineConnExecution
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, LabelExecutor, ResourceExecutor}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import com.webank.wedatasphere.linkis.engineconn.executor.service.ManagerService
import com.webank.wedatasphere.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import com.webank.wedatasphere.linkis.rpc.Sender


class AccessibleEngineConnExecution extends EngineConnExecution with Logging {

  protected def findReportExecutor(engineCreationContext: EngineCreationContext,
                                   engineConn: EngineConn): Executor =
    ExecutorManager.getInstance.getReportExecutor

  protected def beforeReportToLinkisManager(executor: Executor,
                                            engineCreationContext: EngineCreationContext,
                                            engineConn: EngineConn): Unit = {}

  protected def afterReportToLinkisManager(executor: Executor,
                                           engineCreationContext: EngineCreationContext,
                                           engineConn: EngineConn): Unit = {}

  override def execute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    init(engineCreationContext)
    val executor = findReportExecutor(engineCreationContext, engineConn)
    beforeReportToLinkisManager(executor, engineCreationContext, engineConn)
    reportUsedResource(executor, engineCreationContext)
    reportLabel(executor)
    afterReportToLinkisManager(executor, engineCreationContext, engineConn)
  }

  protected def init(engineCreationContext: EngineCreationContext): Unit = {
    val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext()
    listenerBusContext.getEngineConnAsyncListenerBus.start()
  }

  protected def reportUsedResource(executor: Executor, engineCreationContext: EngineCreationContext): Unit = executor match {
    case resourceExecutor: ResourceExecutor =>
      ManagerService.getManagerService
        .reportUsedResource(ResourceUsedProtocol(Sender.getThisServiceInstance,
          resourceExecutor.getCurrentNodeResource(), engineCreationContext.getTicketId))
    case _ =>
      info("Do not need to report usedResource.")
  }

  protected def reportLabel(executor: Executor): Unit = executor match {
    case labelExecutor: LabelExecutor =>
      ManagerService.getManagerService.labelReport(labelExecutor.getExecutorLabels())
      info("Reported all labels to LinkisManager.")
    case _ =>
      info("Do not need to report labels.")
  }

  /**
    * Accessible should be executed by the first, because it will instance the report executor.
    *
    * @return
    */
  override def getOrder: Int = 0
}
