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

package org.apache.linkis.engineconnplugin.flink.executormanager

import org.apache.linkis.engineconn.core.executor.LabelExecutorManagerImpl
import org.apache.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorFactory
import org.apache.linkis.manager.engineplugin.common.creation.CodeLanguageLabelExecutorFactory
import org.apache.linkis.manager.label.entity.Label

class FlinkExecutorManager extends LabelExecutorManagerImpl {

  override def getReportExecutor: Executor = if (getExecutors.isEmpty) {
    val labels = defaultFactory match {
      case _: OnceExecutorFactory =>
        if (null == engineConn.getEngineCreationContext.getLabels()) {
          Array.empty[Label[_]]
        } else {
          engineConn.getEngineCreationContext.getLabels().toArray[Label[_]](Array.empty[Label[_]])
        }
      case labelExecutorFactory: CodeLanguageLabelExecutorFactory =>
        Array[Label[_]](labelExecutorFactory.getDefaultCodeLanguageLabel)
      case _ =>
        if (null == engineConn.getEngineCreationContext.getLabels()) {
          Array.empty[Label[_]]
        } else {
          engineConn.getEngineCreationContext.getLabels().toArray[Label[_]](Array.empty[Label[_]])
        }
    }
    createExecutor(engineConn.getEngineCreationContext, labels)
  } else {
    getExecutors.maxBy {
      case executor: SensibleExecutor => executor.getStatus.ordinal()
      case executor: Executor => executor.getId.hashCode
    }
  }

}
