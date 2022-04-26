/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.sqoop.executor

import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutorExecutionContext}
import org.apache.linkis.engineconnplugin.sqoop.client.LinkisSqoopClient
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.server.toScalaMap
import org.apache.linkis.engineconnplugin.sqoop.client.{LinkisSqoopClient, Sqoop}


trait SqoopOnceExecutor extends ManageableOnceExecutor with SqoopExecutor{
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

  override def getId: String = "SqoopOnceApp_"+ id
  override def close(): Unit = {
    LinkisSqoopClient.close()
    super.close()
  }
  override def trySucceed(): Boolean = {
    super.trySucceed()
    warn(s"$getId has finished with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def tryFailed(): Boolean = {
    LinkisSqoopClient.close()
    super.tryFailed()
    error(s"$getId has failed with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def supportCallBackLogs(): Boolean = true


  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)
}
