/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.seatunnel.executor

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.once.executor.{
  ManageableOnceExecutor,
  OnceExecutorExecutionContext
}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import scala.collection.convert.WrapAsScala._

trait SeatunnelOnceExecutor extends ManageableOnceExecutor with SeatunnelSparkExecutor {

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    doSubmit(onceExecutorExecutionContext, options)
  }

  def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit

  val id: Long

  override def getId: String = "SeatunnelOnceApp_" + id

  override def close(): Unit = {
    super.close()
  }

  override def trySucceed(): Boolean = {
    super.trySucceed()
  }

  override def ensureAvailable[A](f: => A): A = {
    // Not need to throws exception
    Utils.tryQuietly { super.ensureAvailable(f) }
  }

  override def tryFailed(): Boolean = {
    super.tryFailed()
  }

  override def supportCallBackLogs(): Boolean = true

  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)
}
