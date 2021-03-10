/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.creation

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.{EngineConnPluginErrorCode, EngineConnPluginErrorException}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineRunTypeLabel
import com.webank.wedatasphere.linkis.protocol.UserWithCreator


trait ExecutorFactory extends Logging {

  /**
   * Order of executors, the smallest one is the default
   * @return
   */
  def getOrder: Int

  def canCreate(labels: Array[Label[_]]): Boolean = {
    val runTypeLabel = getDefaultEngineRunTypeLabel()
    if (null == runTypeLabel) {
      error("DefaultEngineRunTypeLabel must not be null!")
      throw new EngineConnPluginErrorException(EngineConnPluginErrorCode.INVALID_RUNTYPE, "DefaultEngineRunTypeLabel cannot be null.")
    }
      labels.find(_.isInstanceOf[EngineRunTypeLabel]).foreach {
        case label: EngineRunTypeLabel =>
          info(s"executor runType is ${runTypeLabel.getRunType} input runType is ${label.getRunType}")
          if (runTypeLabel.getRunType.equalsIgnoreCase(label.getRunType)) {
            return true
          }
        case _ =>
          error(s"runType label not exists")
      }
    false
  }

  /**
   *
   * @param engineCreationContext
   * @param engineConn
   * @param labels
   * @return
   */
  def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn, labels: Array[Label[_]]): Executor

  def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel
}

object ExecutorFactory extends Logging {

  def parseUserWithCreator(labels: Array[Label[_]]): UserWithCreator = {
    labels.foreach(l => l match {
      case label: UserWithCreator =>
        return UserWithCreator(label.user, label.creator)
      case _ =>
    })
    null
  }
}