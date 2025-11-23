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

package org.apache.linkis.engineconn.once.executor.creation

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.core.creation.AbstractCodeLanguageLabelExecutorFactory
import org.apache.linkis.engineconn.once.executor.OnceExecutor
import org.apache.linkis.engineconn.once.executor.execution.OnceEngineConnExecution
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineConnMode._
import org.apache.linkis.manager.label.entity.engine.EngineConnModeLabel

trait OnceExecutorFactory extends AbstractCodeLanguageLabelExecutorFactory {

  override protected def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn,
      labels: Array[Label[_]]
  ): OnceExecutor

  override def canCreate(labels: Array[Label[_]]): Boolean =
    super.canCreate(labels) && labels.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode: EngineConnMode = engineConnModeLabel.getEngineConnMode
        OnceEngineConnExecution.getSupportedEngineConnModes.contains(mode)
      case _ => false
    }

}
