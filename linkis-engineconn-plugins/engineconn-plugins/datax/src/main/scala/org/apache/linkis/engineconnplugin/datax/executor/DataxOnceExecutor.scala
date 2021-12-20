package org.apache.linkis.engineconnplugin.datax.executor

import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutorExecutionContext}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.server.toScalaMap

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
