/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.once.executor.creation

import org.apache.linkis.engineconn.core.executor.{ExecutorManager, LabelExecutorManager, LabelExecutorManagerImpl}
import org.apache.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import org.apache.linkis.engineconn.once.executor.OnceExecutor
import org.apache.linkis.engineconn.once.executor.exception.OnceEngineConnErrorException
import org.apache.linkis.manager.label.entity.Label


trait OnceExecutorManager extends LabelExecutorManager {

  override def getReportExecutor: OnceExecutor

}

object OnceExecutorManager {

  private lazy val executorManager = ExecutorManager.getInstance match {
    case manager: OnceExecutorManager => manager
  }

  def getInstance: OnceExecutorManager = executorManager

}

class OnceExecutorManagerImpl extends LabelExecutorManagerImpl with OnceExecutorManager {

  override def getReportExecutor: OnceExecutor = if (getExecutors.isEmpty) {
    val labels = if (null == engineConn.getEngineCreationContext.getLabels()) throw new OnceEngineConnErrorException(12560, "No labels is exists.")
    else engineConn.getEngineCreationContext.getLabels()
    createExecutor(engineConn.getEngineCreationContext, labels.toArray[Label[_]](Array.empty[Label[_]])).asInstanceOf[OnceExecutor]
  } else getExecutors.filter(_.isInstanceOf[OnceExecutor]).maxBy {
    case executor: SensibleExecutor => executor.getStatus.ordinal()
    case executor: Executor => executor.getId.hashCode
  }.asInstanceOf[OnceExecutor]

}
