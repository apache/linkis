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
 
package org.apache.linkis.orchestrator.computation.execute

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.commons.lang.StringUtils

import scala.collection.mutable
import scala.concurrent.duration.Duration


/**
  *
  *
  */
trait CodeExecTaskExecutorManager {


  def createExecutor(execTask: CodeLogicalUnitExecTask): CodeExecTaskExecutor

  def askExecutor(execTask: CodeLogicalUnitExecTask): Option[CodeExecTaskExecutor]

  def askExecutor(execTask: CodeLogicalUnitExecTask,  wait: Duration): Option[CodeExecTaskExecutor]

  def addEngineConnTaskID(executor: CodeExecTaskExecutor): Unit

  def getByEngineConnAndTaskId(serviceInstance: ServiceInstance, engineConnTaskId: String): Option[CodeExecTaskExecutor]

  def getByExecTaskId(execTaskId: String): Option[CodeExecTaskExecutor]

  def shutdown(): Unit

  def delete(execTask: CodeLogicalUnitExecTask, executor: CodeExecTaskExecutor): Unit

  def unLockEngineConn(execTask: CodeLogicalUnitExecTask, execTaskExecutor: CodeExecTaskExecutor): Unit

  def markECFailed(execTask: CodeLogicalUnitExecTask, executor: CodeExecTaskExecutor): Unit

  def getAllInstanceToExecutorCache(): mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]]

  def getAllExecTaskToExecutorCache(): mutable.HashMap[String, CodeExecTaskExecutor]


}

object CodeExecTaskExecutorManager  extends Logging{

  private var codeExecTaskExecutorManager: CodeExecTaskExecutorManager = _

  def getCodeExecTaskExecutorManager: CodeExecTaskExecutorManager = {
    if(codeExecTaskExecutorManager == null) synchronized {
      if(codeExecTaskExecutorManager == null) {
        val orchestratorBuilder = if(StringUtils.isNotBlank(ComputationOrchestratorConf.EXECUTOR_MANAGER_BUILDER_CLASS.getValue))
          ClassUtils.getClassInstance(ComputationOrchestratorConf.EXECUTOR_MANAGER_BUILDER_CLASS.getValue)
        else  new DefaultCodeExecTaskExecutorManager
        info("Use " + orchestratorBuilder.getClass.getName + " to instance a new codeExecTaskExecutorManager.")
        codeExecTaskExecutorManager = orchestratorBuilder
      }
    }
    codeExecTaskExecutorManager
  }

}
