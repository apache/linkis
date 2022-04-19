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
 
package org.apache.linkis.orchestrator.computation.catalyst.reheater

import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.utils.TreeNodeUtil
import org.apache.linkis.orchestrator.core.FailedOrchestrationResponse
import org.apache.linkis.orchestrator.execution.FailedTaskResponse
import org.apache.linkis.orchestrator.extensions.catalyst.ReheaterTransform
import org.apache.linkis.orchestrator.listener.task.TaskLogEvent
import org.apache.linkis.orchestrator.plans.physical.{ExecTask, PhysicalContext, PhysicalOrchestration, ReheatableExecTask, RetryExecTask}
import org.apache.linkis.orchestrator.strategy.ExecTaskStatusInfo

/**
 * Transform physical tree by pruning it's nodes
 *
 */
class PruneTaskRetryTransform extends ReheaterTransform with Logging{

  override def apply(in: ExecTask, context: PhysicalContext): ExecTask = {
      val failedTasks = TreeNodeUtil.getAllFailedTaskNode(in)
      failedTasks.foreach(task => {
        logger.info(s"task:${in.getIDInfo()} has ${failedTasks.size} child tasks which execute failed, some of them may be retried")
        TreeNodeUtil.getTaskResponse(task) match {
          case response: FailedTaskResponse => {
            val exception = response.getCause
            if (exception.isInstanceOf[LinkisRetryException]) {
              val parents = task.getParents
              if (parents != null) {
                parents.foreach(parent => {
                  val otherParents = parents.filter(_ != parent)
                  val otherChildren = parent.getChildren.filter(_ != task)
                  Utils.tryCatch{
                    task match {
                      case retryExecTask: RetryExecTask => {
                        if (retryExecTask.getAge() < ComputationOrchestratorConf.RETRYTASK_MAXIMUM_AGE.getValue) {
                          val newTask = new RetryExecTask(retryExecTask.getOriginTask, retryExecTask.getAge() + 1)
                          newTask.initialize(retryExecTask.getPhysicalContext)
                          TreeNodeUtil.replaceNode(retryExecTask, newTask)
                          pushInfoLog(task, newTask)
                        } else {
                          val logEvent = TaskLogEvent(task, LogUtils.generateWarn(s"Retry task: ${retryExecTask.getIDInfo} reached maximum age:${retryExecTask.getAge()}, stop to retry it!"))
                          task.getPhysicalContext.pushLog(logEvent)
                        }
                      }
                      case _ => {
                        val retryExecTask = new RetryExecTask(task)
                        retryExecTask.initialize(task.getPhysicalContext)
                        TreeNodeUtil.insertNode(parent, task, retryExecTask)
                        pushInfoLog(task, retryExecTask)
                      }
                    }
                  }{
                    //restore task node when retry task construction failed
                    case e: Exception => {
                      val logEvent = TaskLogEvent(task, LogUtils.generateWarn(s"Retry task construction failed, start to restore task node, task node: ${task.getIDInfo}, " +
                        s"age: ${task match { case retryExecTask: RetryExecTask => retryExecTask.getAge() case _ => 0}}, reason: ${e.getMessage}"))
                      logger.error(s"Failed to retry task ${task.getIDInfo()}", e)
                      task.getPhysicalContext.pushLog(logEvent)
                      parent.withNewChildren(otherChildren :+ task)
                      task.withNewParents(otherParents :+ parent)
                      val downLogEvent = TaskLogEvent(task, LogUtils.generateWarn(s"restore task success! task node: ${task.getIDInfo}"))
                      task.getPhysicalContext.pushLog(downLogEvent)
                    }
                  }
                })
              }
            }
          }
          case _ =>
        }
      })
    in
  }

  private def pushInfoLog(task: ExecTask, retryExecTask: RetryExecTask): Unit = {
    val responseOption = TreeNodeUtil.removeTaskResponse(task)
    val stringBuilder = new StringBuilder
    stringBuilder.append(s"This ${task.getIDInfo()} retry, new retry-task is:${retryExecTask.getIDInfo}, retryCount: ${retryExecTask.getAge()}.")
    responseOption.foreach { execTaskStatusInfo =>
      execTaskStatusInfo.taskResponse match {
        case error: FailedOrchestrationResponse =>
          stringBuilder.append("reason:").append(error.getErrorMsg)
        case _ =>
          stringBuilder.append("Reason is empty")
      }
    }
    val logEvent = TaskLogEvent(task, LogUtils.generateInfo(stringBuilder.toString()))
    task.getPhysicalContext.pushLog(logEvent)
  }



  override def getName: String = {
    //Cannot ignore inner class
    getClass.getName
  }
}
