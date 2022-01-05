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
 
package org.apache.linkis.orchestrator.reheater

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.orchestrator.extensions.catalyst.ReheaterTransform
import org.apache.linkis.orchestrator.plans.physical.{ExecTask, ReheatableExecTask}

/**
  *
  *
  */
abstract class AbstractReheater extends Reheater with Logging {

  override def reheat(execTask: ExecTask): Unit = execTask match {
    case reheat: ReheatableExecTask =>
      debug(s"Try to reheat ${execTask.getIDInfo()}.")
      reheat.setReheating()
      var changed = false
      Utils.tryCatch(Option(reheaterTransforms).foreach { transforms =>
        Option(execTask.getChildren).map(_.map{ child =>
          val newChild = transforms.foldLeft(child)((node, transform) => transform.apply(node, execTask.getPhysicalContext))
          if (!child.theSame(newChild)) {
            changed = true
            newChild.relateParents(child)
            newChild
          } else child
        }).foreach { children =>
          if (changed) {
            execTask.withNewChildren(children)
          }
        }
      }) { t =>
        logger.error(s" Reheat ${execTask.getIDInfo()} failed, now mark it failed!", t)
        execTask.getPhysicalContext.markFailed(s" Reheat ${execTask.getIDInfo()} failed, now mark it failed!", t)
      }
      reheat.setReheated()
      if (changed) {
        logger.info(s"${execTask.getIDInfo()} reheated. The physicalTree has been changed. The new tree is ${execTask.simpleString}.")
      }
    case _ =>
  }

  protected def reheaterTransforms: Array[ReheaterTransform]
}
