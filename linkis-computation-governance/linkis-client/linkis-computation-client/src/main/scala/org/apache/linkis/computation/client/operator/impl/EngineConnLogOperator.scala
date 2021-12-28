/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.computation.client.operator.impl

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.computation.client.LinkisJob
import org.apache.linkis.computation.client.once.action.{ECMOperateAction, EngineConnOperateAction}
import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob
import org.apache.linkis.computation.client.operator.OnceJobOperator
import org.apache.linkis.computation.client.utils.LabelKeyUtils
import org.apache.linkis.ujes.client.exception.UJESJobException
import org.apache.commons.lang.StringUtils


class EngineConnLogOperator extends OnceJobOperator[EngineConnLogs]  {

  private var pageSize = 100
  private var fromLine = 1
  private var ignoreKeywords: String = _
  private var onlyKeywords: String = _
  private var engineConnType: String = _
  private var lastRows = 0
  private var ecmServiceInstance: ServiceInstance = _
  private var ecInstance: String = _

  def setEngineConnType(engineConnType: String): Unit = this.engineConnType = engineConnType

  def setECMServiceInstance(serviceInstance: ServiceInstance): Unit = ecmServiceInstance = serviceInstance

  def setPageSize(pageSize: Int): Unit = this.pageSize = pageSize

  def setFromLine(fromLine: Int): Unit = this.fromLine = fromLine

  def setIgnoreKeywords(ignoreKeywords: String): Unit = this.ignoreKeywords = ignoreKeywords

  def setOnlyKeywords(onlyKeywords: String): Unit = this.onlyKeywords = onlyKeywords

  def setLastRows(lastRows: Int): Unit = this.lastRows = lastRows

  /**
    * Try to fetch logs from ECM.
    * @return
    */
  override protected def createOperateActionBuilder(): EngineConnOperateAction.Builder = {
    if (ecInstance == null) {
      ecInstance = getServiceInstance.getInstance
      if (ecmServiceInstance == null) {
        throw new UJESJobException(20310, "ecmServiceInstance must be set!")
      }
      setServiceInstance(ecmServiceInstance)
    }
    ECMOperateAction.newBuilder()
  }


  override def initOperator[U <: LinkisJob](job: U): Unit = job match {
    case submittableSimpleOnceJob: SubmittableSimpleOnceJob =>
      this.ecmServiceInstance = submittableSimpleOnceJob.getECMServiceInstance
      this.engineConnType = submittableSimpleOnceJob.createEngineConnAction.getRequestPayloads.get("labels") match {
        case labels: util.Map[String, String] => labels.get(LabelKeyUtils.ENGINE_TYPE_LABEL_KEY)
      }
    case _ =>
  }

  override protected def addParameters(builder: EngineConnOperateAction.Builder): Unit = {
    builder.addParameter("pageSize", pageSize)
    builder.addParameter("fromLine", fromLine)
    builder.addParameter("ticketId", getTicketId)
    builder.addParameter("creator", getUser)
    builder.addParameter("engineConnInstance", ecInstance)
    if (StringUtils.isNotEmpty(engineConnType)) {
      builder.addParameter("engineConnType", engineConnType)
    }
    if (StringUtils.isNotEmpty(ignoreKeywords)) {
      builder.addParameter("ignoreKeywords", ignoreKeywords)
    }
    if (StringUtils.isNotEmpty(onlyKeywords)) {
      builder.addParameter("onlyKeywords", onlyKeywords)
    }
    if (lastRows > 0) {
      builder.addParameter("lastRows", lastRows)
    }
  }

  override protected def resultToObject(result: EngineConnOperateResult): EngineConnLogs = {
    val endLine: Int = result.getAs("endLine", 0)
    fromLine = endLine + 1
    EngineConnLogs(result.getAs("logPath"), result.getAs("logs"), endLine)
  }

  override def getName: String = EngineConnLogOperator.OPERATOR_NAME

}

object EngineConnLogOperator {
  val OPERATOR_NAME = "engineConnLog"
}

case class EngineConnLogs(logPath: String, logs: util.ArrayList[String], endLine: Int)