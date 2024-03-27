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

package org.apache.linkis.entrance.execute

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf

import org.apache.commons.lang3.StringUtils

import scala.concurrent.duration.Duration

/**
 */
trait CodeExecTaskExecutorManager {

  def createExecutor(execTask: CodeLogicalUnitSimpleExecTask): CodeExecTaskExecutor

  def askExecutor(execTask: CodeLogicalUnitSimpleExecTask): Option[CodeExecTaskExecutor]

  def askExecutor(
      execTask: CodeLogicalUnitSimpleExecTask,
      wait: Duration
  ): Option[CodeExecTaskExecutor]

  def addEngineConnTaskInfo(executor: CodeExecTaskExecutor): Unit

  def getByEngineConnAndTaskId(
      serviceInstance: ServiceInstance,
      engineConnTaskId: String
  ): Option[CodeExecTaskExecutor]

  def getByExecTaskId(execTaskId: String): Option[CodeExecTaskExecutor]

  def shutdown(): Unit

  /**
   * The method of marking task completion externally
   *
   * @param execTask
   * @param executor
   * @param isSucceed
   */
  def markTaskCompleted(
      execTask: CodeLogicalUnitSimpleExecTask,
      executor: CodeExecTaskExecutor,
      isSucceed: Boolean
  ): Unit

  def getAllInstanceToExecutorCache(): java.util.Map[EngineConnTaskInfo, CodeExecTaskExecutor]

  def getAllExecTaskToExecutorCache(): java.util.Map[String, CodeExecTaskExecutor]

  /**
   * The job execution process is normal. After the job is completed, you can call this method. This
   * method will determine the bind engine label. If it is a non-end type job, no operation will be
   * performed.
   *
   * @param execTask
   * @param executor
   */
  protected def delete(
      execTask: CodeLogicalUnitSimpleExecTask,
      executor: CodeExecTaskExecutor
  ): Unit

  /**
   * If the job is executed abnormally, such as execution failure, or being killed, it will go to
   * the process for cleaning up, and the engineConn lock will be released.
   *
   * @param execTask
   * @param execTaskExecutor
   */
  protected def unLockEngineConn(
      execTask: CodeLogicalUnitSimpleExecTask,
      execTaskExecutor: CodeExecTaskExecutor
  ): Unit

  /**
   * Task failed because ec exited unexpectedly, so need to clean up ec immediately
   *
   * @param execTask
   * @param executor
   */
  protected def markECFailed(
      execTask: CodeLogicalUnitSimpleExecTask,
      executor: CodeExecTaskExecutor
  ): Unit

}

object CodeExecTaskExecutorManager extends Logging {

  private var codeExecTaskExecutorManager: CodeExecTaskExecutorManager = _

  def getCodeExecTaskExecutorManager: CodeExecTaskExecutorManager = {
    if (codeExecTaskExecutorManager == null) synchronized {
      if (codeExecTaskExecutorManager == null) {
        val orchestratorBuilder =
          if (
              StringUtils
                .isNotBlank(ComputationOrchestratorConf.EXECUTOR_MANAGER_BUILDER_CLASS.getValue)
          ) {
            ClassUtils.getClassInstance(
              ComputationOrchestratorConf.EXECUTOR_MANAGER_BUILDER_CLASS.getValue
            )
          } else new DefaultCodeExecTaskExecutorManager
        logger.info(
          "Use " + orchestratorBuilder.getClass.getName + " to instance a new codeExecTaskExecutorManager."
        )
        codeExecTaskExecutorManager = orchestratorBuilder
      }
    }
    codeExecTaskExecutorManager
  }

}
