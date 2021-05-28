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
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.physical

import com.webank.wedatasphere.linkis.manager.label.entity.cache.CacheLabel
import com.webank.wedatasphere.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import com.webank.wedatasphere.linkis.orchestrator.core.ResultSet
import com.webank.wedatasphere.linkis.orchestrator.exception.OrchestratorErrorCodeSummary.ORCHESTRATION_FOR_RESPONSE_NOT_SUPPORT_ERROR_CODE
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import com.webank.wedatasphere.linkis.orchestrator.execution._
import com.webank.wedatasphere.linkis.orchestrator.execution.impl.{DefaultFailedTaskResponse, DefaultResultSetTaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{AbstractExecTask, ExecTask, PhysicalContext}
import com.webank.wedatasphere.linkis.orchestrator.utils.OrchestratorIDCreator
import com.webank.wedatasphere.linkis.protocol.query.cache.{CacheNotFound, CacheTaskResult, RequestReadCache, RequestWriteCache}
import com.webank.wedatasphere.linkis.rpc.Sender

import scala.collection.JavaConversions._

class CacheExecTask(parents: Array[ExecTask], children: Array[ExecTask]) extends AbstractExecTask{

  private var physicalContext: PhysicalContext = _

  private var id:String = _

  private var realExecTask: ExecTask = _

  override def canExecute: Boolean = true

  override def execute(): TaskResponse = {
    def dealWithResponse(codeLogicalUnitExecTask: CodeLogicalUnitExecTask, sender: Sender, aSTContext: ASTContext, cacheLabel: CacheLabel, realResponse: TaskResponse): TaskResponse = {
      realResponse match {
        case failed: FailedTaskResponse => throw new OrchestratorErrorException(failed.getErrorCode, failed.getErrorMsg, failed.getCause)
        case resp: ResultSetTaskResponse =>
          val requestWriteCache = new RequestWriteCache(
            codeLogicalUnitExecTask.getCodeLogicalUnit.toStringCode,
            aSTContext.getExecuteUser,
            java.lang.Long.parseLong(cacheLabel.getCacheExpireAfter),
            codeLogicalUnitExecTask.getLabels.map(_.getStringValue),
            resp.getResultSet
          )
          sender.ask(requestWriteCache)
        case async: AsyncTaskResponse =>
          val asyncTaskResponse = async.waitForCompleted()
          dealWithResponse(codeLogicalUnitExecTask, sender, aSTContext, cacheLabel, realResponse = asyncTaskResponse)
        case r => throw new OrchestratorErrorException(ORCHESTRATION_FOR_RESPONSE_NOT_SUPPORT_ERROR_CODE, "Not supported taskResponse " + r)
      }
      realResponse
    }

    realExecTask match {
      case codeLogicalUnitExecTask: CodeLogicalUnitExecTask =>
        val sender = Sender.getSender(ComputationOrchestratorConf.CACHE_SERVICE_APPLICATION_NAME.getValue)
        val aSTContext = codeLogicalUnitExecTask.getTaskDesc.getOrigin.getASTOrchestration.getASTContext
        val cacheLabel = aSTContext.getLabels.find(_.isInstanceOf[CacheLabel]).get.asInstanceOf[CacheLabel]
        val requestReadCache = new RequestReadCache(
          codeLogicalUnitExecTask.getCodeLogicalUnit.toStringCode,
          aSTContext.getExecuteUser,
          codeLogicalUnitExecTask.getLabels.map(_.getStringValue),
          java.lang.Long.parseLong(cacheLabel.getReadCacheBefore)
        )
        sender.ask(requestReadCache) match {
          case cacheTaskResult: CacheTaskResult =>
            //TODO
            new DefaultResultSetTaskResponse(Array(ResultSet(cacheTaskResult.getResultLocation, "_0")))
          case cacheNotFound: CacheNotFound =>
            //new DefaultFailedTaskResponse()
            val realResponse = realExecTask.execute()
            dealWithResponse(codeLogicalUnitExecTask, sender, aSTContext, cacheLabel, realResponse)
        }
      case _ => throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
        "CacheTask does not support " + realExecTask)
    }

  }

  override def isLocalMode: Boolean = true

  def getRealExecTask = realExecTask

  def setRealExecTask(realExecTask: ExecTask) = this.realExecTask = realExecTask

  override def getPhysicalContext: PhysicalContext = physicalContext

  override def initialize(physicalContext: PhysicalContext): Unit = this.physicalContext = physicalContext

  override def verboseString: String = getTaskDesc.toString

  override def getId: String = {
    Option(id).getOrElse{
      id = OrchestratorIDCreator.getPhysicalTaskIDCreator.nextID("cache")
      id
    }
  }

  override protected def newNode(): ExecTask = {
    val cacheExecTask = new CacheExecTask(null, null)
    cacheExecTask.setRealExecTask(this.realExecTask)
    cacheExecTask.setTaskDesc(getTaskDesc)
    cacheExecTask
  }
}

class CacheNotifyListener extends NotifyListener {
  override def apply(taskResponse: TaskResponse): Unit = {

  }
}
