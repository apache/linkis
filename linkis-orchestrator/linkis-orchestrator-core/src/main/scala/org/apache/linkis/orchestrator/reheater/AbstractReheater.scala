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

package org.apache.linkis.orchestrator.reheater

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.extensions.catalyst.ReheaterTransform
import org.apache.linkis.orchestrator.plans.physical.{ExecTask, ReheatableExecTask}

abstract class AbstractReheater extends Reheater with Logging {

  override def reheat(execTask: ExecTask): Boolean = execTask match {
    case reheat: ReheatableExecTask =>
      logger.debug(s"Try to reheat ${execTask.getIDInfo()}")
      reheat.setReheating()
      var changed = false
      Utils.tryCatch(Option(reheaterTransforms).foreach { transforms =>
        execTask.getPhysicalContext.set(OrchestratorConfiguration.REHEATER_KEY, false)
        val newTree = transforms.foldLeft(execTask)((node, transform) =>
          transform.apply(node, execTask.getPhysicalContext)
        )
        val reheaterStatus =
          execTask.getPhysicalContext.get(OrchestratorConfiguration.REHEATER_KEY)
        changed = reheaterStatus match {
          case status: Boolean =>
            status
          case _ => false
        }
        execTask.getPhysicalContext.set(OrchestratorConfiguration.REHEATER_KEY, false)
      }) { t =>
        logger.error(s"Reheat ${execTask.getIDInfo()} failed, now mark it failed", t)
        execTask.getPhysicalContext.markFailed(
          s" Reheat ${execTask.getIDInfo()} failed, now mark it failed!",
          t
        )
      }
      reheat.setReheated()
      if (changed) {
        logger.info(
          s"${execTask.getIDInfo()} reheated. The physicalTree has been changed. The new tree is ${execTask.simpleString}."
        )
      }
      changed
    case _ => false
  }

  protected def reheaterTransforms: Array[ReheaterTransform]
}
