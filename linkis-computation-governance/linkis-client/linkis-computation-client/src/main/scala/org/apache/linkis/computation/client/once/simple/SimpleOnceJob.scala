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

package org.apache.linkis.computation.client.once.simple

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.computation.client.LinkisJobMetrics
import org.apache.linkis.computation.client.job.AbstractSubmittableLinkisJob
import org.apache.linkis.computation.client.once.{LinkisManagerClient, OnceJob, SubmittableOnceJob}
import org.apache.linkis.computation.client.once.action.CreateEngineConnAction
import org.apache.linkis.computation.client.once.result.CreateEngineConnResult
import org.apache.linkis.computation.client.operator.OnceJobOperator

import java.util.Locale

import scala.concurrent.duration.Duration

trait SimpleOnceJob extends OnceJob {

  protected var lastEngineConnState: String = _
  protected var lastNodeInfo: java.util.Map[String, Any] = _
  private var finalEngineConnState: String = _
  protected var isRunning: Boolean = false

  override def getId: String = wrapperEC(engineConnId)

  override def isCompleted: Boolean = wrapperEC {
    if (finalEngineConnState != null) return true
    lastNodeInfo = getNodeInfo
    lastEngineConnState = getStatus(lastNodeInfo)
    isCompleted(lastEngineConnState)
  }

  protected def isCompleted(status: String): Boolean = status.toLowerCase(Locale.getDefault) match {
    case "success" | "failed" | "shuttingdown" =>
      finalEngineConnState = status
      getJobListeners.foreach(_.onJobFinished(this))
      true
    case "unlock" | "running" | "idle" | "busy" =>
      if (!isRunning) {
        isRunning = true
        getJobListeners.foreach(_.onJobRunning(this))
      }
      false
    case _ => false
  }

  def getStatus: String = lastEngineConnState

  override def isSucceed: Boolean = wrapperEC {
    finalEngineConnState.toLowerCase(Locale.getDefault) match {
      case "success" => true
      case _ => false
    }
  }

  protected def transformToId(): Unit = {
    engineConnId =
      s"${ticketId.length}_${serviceInstance.getApplicationName.length}_${ticketId}${serviceInstance.getApplicationName}${serviceInstance.getInstance}"
  }

  protected def transformToServiceInstance(): Unit = engineConnId match {
    case SimpleOnceJob.ENGINE_CONN_ID_REGEX(ticketIdLen, appNameLen, serviceInstanceStr) =>
      val index1 = ticketIdLen.toInt
      val index2 = index1 + appNameLen.toInt
      ticketId = serviceInstanceStr.substring(0, index1)
      serviceInstance = ServiceInstance(
        serviceInstanceStr.substring(index1, index2),
        serviceInstanceStr.substring(index2)
      )
  }

  protected def initOnceOperatorActions(): Unit = addOperatorAction {
    case onceJobOperator: OnceJobOperator[_] =>
      onceJobOperator
        .setUser(user)
        .setTicketId(ticketId)
        .setServiceInstance(serviceInstance)
        .setLinkisManagerClient(linkisManagerClient)
    case operator => operator
  }

  def getEcServiceInstance: ServiceInstance = serviceInstance

  def getEcTicketId: String = ticketId

}

class SubmittableSimpleOnceJob(
    protected override val linkisManagerClient: LinkisManagerClient,
    val createEngineConnAction: CreateEngineConnAction
) extends SimpleOnceJob
    with SubmittableOnceJob
    with AbstractSubmittableLinkisJob {

  private var ecmServiceInstance: ServiceInstance = _
  private var createEngineConnResult: CreateEngineConnResult = _

  def getECMServiceInstance: ServiceInstance = ecmServiceInstance
  def getCreateEngineConnResult: CreateEngineConnResult = createEngineConnResult

  override protected def doSubmit(): Unit = {
    logger.info(s"Ready to create a engineConn: ${createEngineConnAction.getRequestPayload}.")
    createEngineConnResult = linkisManagerClient.createEngineConn(createEngineConnAction)
    lastNodeInfo = createEngineConnResult.getNodeInfo
    serviceInstance = getServiceInstance(lastNodeInfo)
    ticketId = getTicketId(lastNodeInfo)
    ecmServiceInstance = getECMServiceInstance(lastNodeInfo)
    lastEngineConnState = getStatus(lastNodeInfo)
    logger.info(
      s"EngineConn created with status $lastEngineConnState, the nodeInfo is $lastNodeInfo."
    )
    initOnceOperatorActions()
    if (!isCompleted(lastEngineConnState) && !isRunning) {
      logger.info(s"Wait for EngineConn $serviceInstance to be running or completed.")
      Utils.waitUntil(() => isCompleted || isRunning, Duration.Inf)
      serviceInstance = getServiceInstance(lastNodeInfo)
      logger.info(s"EngineConn of $serviceInstance is in $lastEngineConnState.")
      transformToId()
    } else {
      logger.info(s"EngineConn $serviceInstance is aleady running, transform to id")
      transformToId()
    }
  }

  override protected val user: String = createEngineConnAction.getUser
}

class ExistingSimpleOnceJob(
    protected override val linkisManagerClient: LinkisManagerClient,
    id: String,
    override protected val user: String
) extends SimpleOnceJob {
  engineConnId = id
  transformToServiceInstance()
  initOnceOperatorActions()
  private val jobMetrics: LinkisJobMetrics = new LinkisJobMetrics(id)

  override def getJobMetrics: LinkisJobMetrics = jobMetrics
}

object SimpleOnceJob {

  private val ENGINE_CONN_ID_REGEX = "(\\d+)_(\\d+)_(.+)".r

  def builder(): SimpleOnceJobBuilder = new SimpleOnceJobBuilder

  /**
   * Build a submitted SimpleOnceJob by id and user.
   * @param id
   *   the id of submitted SimpleOnceJob
   * @param user
   *   the execution user of submitted SimpleOnceJob
   * @return
   */
  def build(id: String, user: String): SimpleOnceJob =
    new ExistingSimpleOnceJob(SimpleOnceJobBuilder.getLinkisManagerClient, id, user)

}
