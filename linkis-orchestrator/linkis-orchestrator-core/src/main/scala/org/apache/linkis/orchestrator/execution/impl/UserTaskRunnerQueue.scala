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

package org.apache.linkis.orchestrator.execution.impl

import org.apache.linkis.orchestrator.Orchestrator
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.execution.ExecTaskRunner
import org.apache.linkis.orchestrator.plugin.UserParallelOrchestratorPlugin

import scala.collection.mutable

trait UserTaskRunnerQueue {

  def add(taskRunner: ExecTaskRunner, userRunningNumber: UserRunningNumber): Unit

  def addAll(taskRunners: Array[ExecTaskRunner], userRunningNumber: UserRunningNumber): Unit

  def takeTaskRunner(max: Int): Array[UserTaskRunner]

  def takeTaskRunnerAll(): Array[UserTaskRunner]

}

class UserTaskRunnerPriorityQueue extends UserTaskRunnerQueue {

  private implicit val ord: Ordering[UserTaskRunner] = Ordering.by(_.getScore())

  private val priorityQueue = new mutable.PriorityQueue[UserTaskRunner]()

  private val DEFAULT_MAX_RUNNING =
    OrchestratorConfiguration.ORCHESTRATOR_USER_MAX_RUNNING.getValue

  private val userParallelOrchestratorPlugin =
    Orchestrator.getOrchestrator.getOrchestratorContext.getOrchestratorPlugins
      .find(_.isInstanceOf[UserParallelOrchestratorPlugin])
      .map(_.asInstanceOf[UserParallelOrchestratorPlugin])

  override def takeTaskRunner(max: Int): Array[UserTaskRunner] = {
    val arr = priorityQueue.dequeueAll.toArray
    if (arr.length > max) {
      arr.splitAt(max)._1
    } else {
      arr
    }
  }

  override def takeTaskRunnerAll(): Array[UserTaskRunner] = {
    priorityQueue.dequeueAll
  }

  override def add(taskRunner: ExecTaskRunner, userRunningNumber: UserRunningNumber): Unit = {
    val astContext = taskRunner.task.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
    val user = astContext.getExecuteUser
    val labels = astContext.getLabels
    val maxRunningNumber =
      if (userParallelOrchestratorPlugin.isDefined) {
        userParallelOrchestratorPlugin.get.getUserMaxRunningJobs(user, labels)
      } else {
        DEFAULT_MAX_RUNNING
      }
    val runningNumber = userRunningNumber.getRunningNumber(user, labels)
    priorityQueue += UserTaskRunner(user, maxRunningNumber, runningNumber, taskRunner)
  }

  override def addAll(
      taskRunners: Array[ExecTaskRunner],
      userRunningNumber: UserRunningNumber
  ): Unit = {
    val runners = taskRunners
      .map { taskRunner =>
        val astContext = taskRunner.task.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
        val user = astContext.getExecuteUser
        val labels = astContext.getLabels
        val maxRunningNumber =
          if (userParallelOrchestratorPlugin.isDefined) {
            userParallelOrchestratorPlugin.get.getUserMaxRunningJobs(user, labels)
          } else {
            DEFAULT_MAX_RUNNING
          }
        UserTaskRunner(
          user,
          maxRunningNumber,
          userRunningNumber.addNumber(user, labels),
          taskRunner
        )
      }
      .filter(userTaskRunner => userTaskRunner.maxRunningNumber > userTaskRunner.runningNumber)
    priorityQueue ++= runners
  }

}
