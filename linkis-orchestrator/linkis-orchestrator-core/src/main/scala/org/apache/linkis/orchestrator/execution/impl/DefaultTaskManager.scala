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

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.execution._
import org.apache.linkis.orchestrator.listener.OrchestratorSyncEvent
import org.apache.linkis.orchestrator.listener.execution.ExecutionTaskCompletedEvent
import org.apache.linkis.orchestrator.listener.task._
import org.apache.linkis.orchestrator.plans.physical.ExecTask

import java.util
import java.util.concurrent.CopyOnWriteArrayList

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 */
class DefaultTaskManager extends AbstractTaskManager with Logging {

  /**
   * executionTasks
   */
  private val executionTasks: util.List[ExecutionTask] = new CopyOnWriteArrayList[ExecutionTask]()

  /**
   * key: execTaskID value: ExecutionTask in running
   */
  private val execTaskToExecutionTask: mutable.Map[String, ExecutionTask] =
    new mutable.HashMap[String, ExecutionTask]()

  private val execTaskToExecutionTaskWriteLock = new Array[Byte](0)

  /**
   * key: ExecutionTaskID value: Array ExecTaskRunner in running
   */
  private val executionTaskToRunningExecTask: mutable.Map[String, ArrayBuffer[ExecTaskRunner]] =
    new mutable.HashMap[String, ArrayBuffer[ExecTaskRunner]]()

  /**
   * key: ExecutionTaskID value: Array ExecTaskRunner in completed
   */
  private val executionTaskToCompletedExecTask: mutable.Map[String, ArrayBuffer[ExecTaskRunner]] =
    new mutable.HashMap[String, ArrayBuffer[ExecTaskRunner]]()

  private val MAX_RUNNER_TASK_SIZE = OrchestratorConfiguration.TASK_RUNNER_MAX_SIZE.getValue

  // private val syncListenerBus: OrchestratorSyncListenerBus = OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorSyncListenerBus

  private val userRunningNumber: UserRunningNumber = new UserRunningNumber

  /**
   * create ExecutionTask
   *
   * @param task
   * @return
   */
  override def putExecTask(task: ExecTask): ExecutionTask = {
    if (null != task) {
      val executionTask = new BaseExecutionTask(
        OrchestratorConfiguration.EXECUTION_TASK_MAX_PARALLELISM.getValue,
        task
      )
      executionTasks.add(executionTask)
      execTaskToExecutionTaskWriteLock synchronized {
        execTaskToExecutionTask.put(task.getId, executionTask)
      }
      logger.info(
        s"submit execTask ${task.getIDInfo()} to taskManager get executionTask ${executionTask.getId}"
      )
      task.getPhysicalContext.broadcastAsyncEvent(TaskConsumerEvent(task))
      return executionTask
    }
    null
  }

  def getRunningExecutionTasks: Array[String] =
    executionTaskToRunningExecTask.keysIterator.toArray

  override def getRunningTask(executionTaskId: String): Array[ExecTaskRunner] = {
    executionTaskToRunningExecTask.get(executionTaskId).map(_.toArray).getOrElse(Array.empty)
  }

  override def getRunningTask(task: ExecTask): Array[ExecTaskRunner] = {
    val executionTask = execTaskToExecutionTask.getOrElse(task.getId, null)
    if (null != executionTask) {
      executionTaskToRunningExecTask
        .get(executionTask.getId)
        .map(_.toArray)
        .getOrElse(Array.empty)
    } else {
      Array.empty
    }
  }

  override def getCompletedTasks(executionTaskId: String): Array[ExecTaskRunner] =
    executionTaskToCompletedExecTask.get(executionTaskId).map(_.toArray).getOrElse(Array.empty)

  override def getCompletedTasks(task: ExecTask): Array[ExecTaskRunner] = execTaskToExecutionTask
    .get(task.getId)
    .map(executionTask =>
      executionTaskToCompletedExecTask
        .get(executionTask.getId)
        .map(_.toArray)
        .getOrElse(Array.empty)
    )
    .getOrElse(Array.empty)

  def getRunnableExecutionTasks: Array[ExecutionTask] = getSuitableExecutionTasks.filter {
    executionTask =>
      val execTask = executionTask.getRootExecTask
      val subTasks = new mutable.HashSet[ExecTask]()
      getSubTasksRecursively(executionTask, execTask, subTasks)
      subTasks.nonEmpty
  }

  def getRunnableExecutionTasksAndExecTask: (Array[ExecutionTask], Array[ExecTaskRunner]) = {
    val execTaskRunners = ArrayBuffer[ExecTaskRunner]()
    val runningExecutionTasks = getSuitableExecutionTasks.filter { executionTask =>
      val execTask = executionTask.getRootExecTask
      val runnableSubTasks = new mutable.HashSet[ExecTask]()
      getSubTasksRecursively(executionTask, execTask, runnableSubTasks)
      if (runnableSubTasks.nonEmpty) {
        val subExecTaskRunners = runnableSubTasks.map(execTaskToTaskRunner)
        execTaskRunners ++= subExecTaskRunners
        true
      } else {
        false
      }
    }
    (runningExecutionTasks, execTaskRunners.toArray)
  }

  protected def getSuitableExecutionTasks: Array[ExecutionTask] = {
    executionTasks.asScala
      .filter(executionTask =>
        executionTask.getRootExecTask.canExecute
          && !ExecutionNodeStatus.isCompleted(executionTask.getStatus)
      )
      .toArray
  }

  override def taskRunnableTasks(execTaskRunners: Array[ExecTaskRunner]): Array[ExecTaskRunner] = {
    // 2. Take the current maximum number of runnables from the priority queue: Maximum limit-jobs that are already running
    val nowRunningNumber = executionTaskToRunningExecTask.values.map(_.length).sum
    val maxRunning =
      if (nowRunningNumber >= MAX_RUNNER_TASK_SIZE) 0 else MAX_RUNNER_TASK_SIZE - nowRunningNumber
    if (maxRunning == 0) {
      logger.warn(s"The current running has exceeded the maximum, now: $nowRunningNumber ")
      Array.empty[ExecTaskRunner]
    } else if (execTaskRunners.isEmpty) {
      logger.debug("There are no tasks to run now")
      Array.empty[ExecTaskRunner]
    } else {
      // 3. create priorityQueue Scoring rules: End type tasks are 100 points, userMax-runningNumber (remaining ratio) is additional points
      val userTaskRunnerQueue = new UserTaskRunnerPriorityQueue
      userTaskRunnerQueue.addAll(execTaskRunners.toArray, userRunningNumber.copy())
      val userTaskRunners = userTaskRunnerQueue.takeTaskRunner(maxRunning)
      val runners = new ArrayBuffer[ExecTaskRunner]()
      userTaskRunners.foreach { userTaskRunner =>
        val execTask = userTaskRunner.taskRunner.task
        val executionTask =
          execTaskToExecutionTask.get(execTask.getPhysicalContext.getRootTask.getId)
        if (executionTask.isDefined) {
          val executionTaskId = executionTask.get.getId
          executionTaskToRunningExecTask synchronized {
            val runningExecTaskRunner =
              if (!executionTaskToRunningExecTask.contains(executionTaskId)) {
                val taskRunnerBuffer = new ArrayBuffer[ExecTaskRunner]()
                executionTaskToRunningExecTask.put(executionTaskId, taskRunnerBuffer)
                val astContext = execTask.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
                // Running Execution task add
                val oldNumber =
                  userRunningNumber.addNumber(astContext.getExecuteUser, astContext.getLabels)
                logger.info(
                  s"user key ${userRunningNumber.getKey(astContext.getLabels, astContext.getExecuteUser)}, " +
                    s"executionTaskId $executionTaskId to addNumber: ${oldNumber + 1}"
                )
                taskRunnerBuffer
              } else {
                executionTaskToRunningExecTask(executionTaskId)
              }
            runningExecTaskRunner.append(userTaskRunner.taskRunner)
            runners += userTaskRunner.taskRunner
          }

        }
      }
      runners.toArray
    }
  }

  /**
   * Get runnable TaskRunner
   *   1. Polling for all outstanding ExecutionTasks 2. Polling for unfinished subtasks of
   *      ExecutionTask corresponding to ExecTask tree 3. Get the subtask and determine whether it
   *      exceeds the maximum value of getRunnable. If it exceeds the maximum value, the maximum
   *      number of tasks will be returned
   *
   * @return
   */
  override def getRunnableTasks: Array[ExecTaskRunner] = {
    val startTime = System.currentTimeMillis()
    logger.debug(s"Start to getRunnableTasks startTime: $startTime")
    val execTaskRunners = ArrayBuffer[ExecTaskRunner]()
    val runningExecutionTasks = getSuitableExecutionTasks
    // 1. Get all runnable TaskRunner
    runningExecutionTasks.foreach { executionTask =>
      val execTask = executionTask.getRootExecTask
      val runnableSubTasks = new mutable.HashSet[ExecTask]()
      getSubTasksRecursively(executionTask, execTask, runnableSubTasks)
      val subExecTaskRunners = runnableSubTasks.map(execTaskToTaskRunner)
      execTaskRunners ++= subExecTaskRunners
    }
    val finishTime = System.currentTimeMillis()
    logger.debug(
      s"Finished to getRunnableTasks finishTime: $finishTime," +
        s"taken: ${ByteTimeUtils.msDurationToString(finishTime - startTime)}"
    )
    taskRunnableTasks(execTaskRunners.toArray)
  }

  /**
   * Modified TaskRunner The runningExecTaskMap needs to be removed and cleaned Need to increase the
   * difference of completedExecTaskMap
   *
   * @param task
   */
  override def addCompletedTask(task: ExecTaskRunner): Unit = {
    logger.info(s"${task.task.getIDInfo()} task completed, now remove from taskManager")
    val rootTask = task.task.getPhysicalContext.getRootTask
    val astContext = rootTask.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
    execTaskToExecutionTask.get(rootTask.getId).foreach { executionTask =>
      // put completed execTasks to completed collections
      executionTaskToCompletedExecTask synchronized {
        val completedRunners = executionTaskToCompletedExecTask
          .getOrElseUpdate(executionTask.getId, new ArrayBuffer[ExecTaskRunner]())
        if (!completedRunners.exists(_.task.getId.equals(task.task.getId))) {
          completedRunners += task
        } else {
          logger.error(s"Task${task.task.getIDInfo()} has completed, but has in completed")
        }
      }

      executionTaskToRunningExecTask synchronized {
        val oldRunningTasks = executionTaskToRunningExecTask.getOrElse(executionTask.getId, null)
        // from running ExecTasks to remove completed execTasks
        var shouldMinusTaskNumber = false
        if (null != oldRunningTasks) {
          val runningRunners =
            oldRunningTasks.filterNot(runner => task.task.getId.equals(runner.task.getId))
          if (runningRunners.isEmpty) {
            executionTaskToRunningExecTask.remove(executionTask.getId)
            shouldMinusTaskNumber = true
          } else {
            executionTaskToRunningExecTask.put(executionTask.getId, runningRunners)
          }
        } else {
          shouldMinusTaskNumber = true
        }
        if (shouldMinusTaskNumber) {
          val oldNumber =
            userRunningNumber.minusNumber(astContext.getExecuteUser, astContext.getLabels)
          logger.info(s"executionTask(${executionTask.getId}) no task running, user key ${userRunningNumber
            .getKey(astContext.getLabels, astContext.getExecuteUser)}, minusNumber: ${oldNumber - 1}")
        }
      }

    }
    rootTask.getPhysicalContext.broadcastAsyncEvent(TaskConsumerEvent(task.task))
  }

  /**
   * TODO executionTaskAndRootExecTask Will clean up, the data is not the most complete, you need to
   * consider storing the removed to the persistence layer
   *
   * @return
   */
  override def pollCompletedExecutionTasks: Array[ExecutionTask] = {
    executionTasks.asScala
      .filter(executionTask => ExecutionNodeStatus.isCompleted(executionTask.getStatus))
      .toArray
  }

  /**
   * Recursively obtain tasks that can be run under ExecutionTask
   *   1. First judge whether the child node of the corresponding node is completed, and if the
   *      operation is completed, submit the node (recursive exit condition 1) 2. If there are child
   *      nodes that are not completed, submit the child nodes recursively 3. If the obtained task
   *      exceeds the maximum concurrent number of ExecutionTask, return directly (recursive exit
   *      condition 2) TODO Whether needs to do strict maximum task concurrency control, exit
   *      condition 2 also needs to consider the task currently running
   *
   * @param executionTask
   * @param execTask
   * @param subTasks
   */
  private def getSubTasksRecursively(
      executionTask: ExecutionTask,
      execTask: ExecTask,
      subTasks: mutable.Set[ExecTask]
  ): Unit = {
    if (subTasks.size > executionTask.getMaxParallelism || isExecuted(executionTask, execTask)) {
      return
    }
    val tasks = findUnCompletedExecTasks(executionTask.getId, execTask.getChildren)
    if (null == tasks || tasks.isEmpty) {
      if (execTask.canExecute) {
        subTasks.add(execTask)
      }
    } else {
      // Recursive child node
      tasks.foreach(getSubTasksRecursively(executionTask, _, subTasks))
    }
  }

  private def isExecuted(executionTask: ExecutionTask, execTask: ExecTask): Boolean = {
    val runningDefined = executionTaskToRunningExecTask
      .get(executionTask.getId)
      .exists(_.exists(_.task.getId.equals(execTask.getId)))
    val completedDefined = executionTaskToCompletedExecTask
      .get(executionTask.getId)
      .exists(_.exists(_.task.getId.equals(execTask.getId)))
    runningDefined || completedDefined
  }

  /**
   * from tasks to find unCompleted ExecTasks
   *
   * @param executionTaskId
   * @param tasks
   * @return
   */
  private def findUnCompletedExecTasks(
      executionTaskId: String,
      tasks: Array[ExecTask]
  ): Array[ExecTask] = {
    val maybeRunners = executionTaskToCompletedExecTask.get(executionTaskId)
    if (maybeRunners.isDefined) {
      val completedTask = maybeRunners.get
      tasks.filter(execTask => !completedTask.exists(_.task.getId.equals(execTask.getId)))
    } else {
      tasks
    }
  }

  protected def execTaskToTaskRunner(execTask: ExecTask): ExecTaskRunner = {
    val execTaskRunner = ExecTaskRunner.getExecTaskRunnerFactory.createExecTaskRunner(execTask)
    execTaskRunner
  }

  override def onSyncEvent(event: OrchestratorSyncEvent): Unit = event match {
    case rootTaskResponseEvent: RootTaskResponseEvent =>
      onRootTaskResponseEvent(rootTaskResponseEvent)
    case _ =>

  }

  private def clearExecutionTask(executionTask: ExecutionTask): Unit = {

    val task = executionTask.getRootExecTask
    val astContext = task.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
    logger.info(s"executionTask(${executionTask.getId}) finished user key ${userRunningNumber
      .getKey(astContext.getLabels, astContext.getExecuteUser)}")
    // from executionTask to remove executionTask
    executionTasks.remove(executionTask)
    // from execTaskToExecutionTask to remove root execTask
    execTaskToExecutionTaskWriteLock synchronized {
      execTaskToExecutionTask.remove(task.getId)
    }
    // from executionTaskToCompletedExecTask to remove executionTask
    executionTaskToCompletedExecTask synchronized {
      executionTaskToCompletedExecTask.remove(executionTask.getId)
    }
    val maybeRunners = executionTaskToRunningExecTask synchronized {
      executionTaskToRunningExecTask.remove(executionTask.getId)
    }
    if (maybeRunners.isDefined) {
      val oldNumber =
        userRunningNumber.minusNumber(astContext.getExecuteUser, astContext.getLabels)
      logger.info(s"executionTask(${executionTask.getId}) finished user key ${userRunningNumber
        .getKey(astContext.getLabels, astContext.getExecuteUser)}, minusNumber: ${oldNumber - 1}")
    }
  }

  override def onRootTaskResponseEvent(rootTaskResponseEvent: RootTaskResponseEvent): Unit = {
    logger.info(s"received rootTaskResponseEvent ${rootTaskResponseEvent.execTask.getIDInfo()}")
    val rootTask = rootTaskResponseEvent.execTask
    val maybeTask = execTaskToExecutionTask.get(rootTask.getId)
    if (maybeTask.isDefined) {
      val executionTask = maybeTask.get
      rootTaskResponseEvent.taskResponse match {
        case failedTaskResponse: FailedTaskResponse =>
          markExecutionTaskCompleted(executionTask, failedTaskResponse)
        case succeedTaskResponse: SucceedTaskResponse =>
          markExecutionTaskCompleted(executionTask, succeedTaskResponse)
      }
    }

  }

  override protected def markExecutionTaskCompleted(
      executionTask: ExecutionTask,
      taskResponse: CompletedTaskResponse
  ): Unit = {
    logger.debug(
      s"Start to mark executionTask(${executionTask.getId}) rootExecTask ${executionTask.getRootExecTask.getIDInfo()} to  Completed."
    )
    clearExecutionTask(executionTask)
    executionTask.getRootExecTask.getPhysicalContext.broadcastSyncEvent(
      ExecutionTaskCompletedEvent(executionTask.getId, taskResponse)
    )
    logger.info(
      s"Finished to mark executionTask(${executionTask.getId}) rootExecTask ${executionTask.getRootExecTask.getIDInfo()} to  Completed."
    )
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}

}
