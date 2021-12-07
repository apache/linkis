package com.webank.wedatasphere.linkis.engineconnplugin.datax.executor

import com.alibaba.datax.DataxEngine
import com.webank.wedatasphere.linkis.engineconn.core.hook.ShutdownHook
import com.webank.wedatasphere.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutorExecutionContext}
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.server.toScalaMap

trait DataxOnceExecutor extends ManageableOnceExecutor with DataxExecutor {
  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    doSubmit(onceExecutorExecutionContext, options)
  }

  def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit

  val id: Long

  override def getId: String = "DataxOnceApp_" + id

  override def close(): Unit = {
    super.close()
  }

  override def trySucceed(): Boolean = {
    super.trySucceed()
    warn(s"$getId has finished with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def tryFailed(): Boolean = {
    super.tryFailed()
    error(s"$getId has failed with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def supportCallBackLogs(): Boolean = true


  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)
}
