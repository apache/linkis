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
package com.webank.wedatasphere.linkis.computation.client.once.simple

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.computation.client.LinkisJobMetrics
import com.webank.wedatasphere.linkis.computation.client.job.AbstractSubmittableLinkisJob
import com.webank.wedatasphere.linkis.computation.client.once.action.CreateEngineConnAction
import com.webank.wedatasphere.linkis.computation.client.once.{LinkisManagerClient, OnceJob, SubmittableOnceJob}
import com.webank.wedatasphere.linkis.computation.client.operator.OnceJobOperator

import scala.concurrent.duration.Duration


trait SimpleOnceJob extends OnceJob {

  protected var lastEngineConnState: String = _
  protected var lastNodeInfo: java.util.Map[String, Any] = _
  private var finalEngineConnState: String = _
  protected var isRunning: Boolean = false

  override def getId: String = wrapperEC(engineConnId)

  override def isCompleted: Boolean = wrapperEC {
    if(finalEngineConnState != null) return true
    lastNodeInfo = getNodeInfo
    lastEngineConnState = getStatus(lastNodeInfo)
    isCompleted(lastEngineConnState)
  }

  protected def isCompleted(status: String): Boolean = status.toLowerCase match {
    case "success" | "failed" =>
      finalEngineConnState = status
      getJobListeners.foreach(_.onJobFinished(this))
      true
    case "unlock" | "running" | "idle" | "busy" =>
      if(!isRunning) {
        isRunning = true
        getJobListeners.foreach(_.onJobRunning(this))
      }
      false
    case _ => false
  }

  def getStatus: String = lastEngineConnState

  override def isSucceed: Boolean = wrapperEC {
    finalEngineConnState.toLowerCase match {
      case "success" => true
      case _ => false
    }
  }

  protected def transformToId(): Unit = {
    engineConnId = serviceInstance.getApplicationName.length + serviceInstance.getApplicationName + serviceInstance.getInstance
  }

  protected def transformToServiceInstance(): Unit = engineConnId match {
    case SimpleOnceJob.ENGINE_CONN_ID_REGEX(len, serviceInstanceStr) =>
      val length = len.toInt
      serviceInstance = ServiceInstance(serviceInstanceStr.substring(0, length), serviceInstanceStr.substring(length))
  }

}

class SubmittableSimpleOnceJob(protected override val linkisManagerClient: LinkisManagerClient,
                               createEngineConnAction: CreateEngineConnAction)
  extends SimpleOnceJob with SubmittableOnceJob with AbstractSubmittableLinkisJob {
  override protected def doSubmit(): Unit = {
    info(s"Ready to create a engineConn: ${createEngineConnAction.getRequestPayload}.")
    val nodeInfo = linkisManagerClient.createEngineConn(createEngineConnAction)
    lastNodeInfo = nodeInfo.getNodeInfo
    serviceInstance = getServiceInstance(lastNodeInfo)
    lastEngineConnState = getStatus(lastNodeInfo)
    info(s"EngineConn created with status $lastEngineConnState, the nodeInfo is $lastNodeInfo.")
    addOperatorAction {
      case onceJobOperator: OnceJobOperator[_] =>
        onceJobOperator.setServiceInstance(serviceInstance).setLinkisManagerClient(linkisManagerClient)
      case operator => operator
    }
    if(!isCompleted(lastEngineConnState) && !isRunning) {
      info(s"Wait for EngineConn $serviceInstance to be running or completed.")
      Utils.waitUntil(() => isCompleted || isRunning, Duration.Inf)
      serviceInstance = getServiceInstance(lastNodeInfo)
      info(s"EngineConn of $serviceInstance is in $lastEngineConnState.")
      transformToId()
    }else{
      info(s"EngineConn $serviceInstance is aleady running, transform to id")
      transformToId()
    }
  }
  override protected val user: String = createEngineConnAction.getUser
}

class ExistingSimpleOnceJob(protected override val linkisManagerClient: LinkisManagerClient,
                            id: String, override protected val user: String) extends SimpleOnceJob {
  engineConnId = id
  transformToServiceInstance()
  private val jobMetrics: LinkisJobMetrics = new LinkisJobMetrics(id)

  override def getJobMetrics: LinkisJobMetrics = jobMetrics
}

object SimpleOnceJob {

  private val ENGINE_CONN_ID_REGEX = "(\\d+)(.+)".r

  def builder(): SimpleOnceJobBuilder = new SimpleOnceJobBuilder

  /**
    * Build a submitted SimpleOnceJob by id and user.
    * @param id the id of submitted SimpleOnceJob
    * @param user the execution user of submitted SimpleOnceJob
    * @return
    */
  def build(id: String, user: String): SimpleOnceJob = new ExistingSimpleOnceJob(SimpleOnceJobBuilder.getLinkisManagerClient, id, user)

}