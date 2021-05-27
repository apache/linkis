package com.webank.wedatasphere.linkis.engineconn.computation.executor.creation

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, RetryHandler, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.core.executor.{ExecutorManager, LabelExecutorManager, LabelExecutorManagerImpl}
import com.webank.wedatasphere.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.CodeLanguageLabel

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2021/5/1.
  */
trait ComputationExecutorManager extends LabelExecutorManager {

  def getDefaultExecutor: ComputationExecutor

  override def getReportExecutor: ComputationExecutor

}

object ComputationExecutorManager {

  DataWorkCloudApplication.setProperty(EngineConnExecutorConfiguration.EXECUTOR_MANAGER_CLASS.key,
    "com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl")

  private val executorManager = ExecutorManager.getInstance match {
    case manager: ComputationExecutorManager => manager
  }

  def getInstance: ComputationExecutorManager = executorManager

}

class ComputationExecutorManagerImpl extends LabelExecutorManagerImpl with ComputationExecutorManager with Logging {

  private var defaultExecutor: ComputationExecutor = _

  override def getDefaultExecutor: ComputationExecutor = {
    if(defaultExecutor != null) return defaultExecutor
    synchronized {
      if (null == defaultExecutor || defaultExecutor.isClosed) {
        val engineConn = EngineConnManager.getEngineConnManager.getEngineConn
        if (null == engineConn) {
          Utils.waitUntil(() => null != engineConn, Duration.apply(EngineConnConf.ENGINE_CONN_CREATION_WAIT_TIME.getValue.toLong, TimeUnit.MILLISECONDS))
          error(s"Create default executor failed, engineConn not ready after ${EngineConnConf.ENGINE_CONN_CREATION_WAIT_TIME.getValue.toString}.")
          return null
        }
        val retryHandler = new RetryHandler {}
        retryHandler.addRetryException(classOf[ErrorException]) // Linkis exception will retry.
        defaultExecutor = retryHandler.retry(createExecutor(engineConn.getEngineCreationContext), "Create default executor") match {
          case computationExecutor: ComputationExecutor => computationExecutor
        }
      }
      defaultExecutor
    }
  }

  override def getReportExecutor: ComputationExecutor = if(getExecutors.isEmpty) getDefaultExecutor
  else getExecutors.maxBy {
    case computationExecutor: ComputationExecutor => computationExecutor.getLastActivityTime
  }.asInstanceOf[ComputationExecutor]

  override protected def getLabelKey(labels: Array[Label[_]]): String = {
    labels.foreach {
      case label: CodeLanguageLabel =>
        return label.getCodeType
      case _ =>
    }
    error("Cannot get label key. labels : " + GSON.toJson(labels))
    null
  }
}