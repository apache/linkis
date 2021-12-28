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
 
package org.apache.linkis.computation.client.once

import org.apache.linkis.computation.client.job.{AbstractLinkisJob, SubmittableLinkisJob}
import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.computation.client.once.action.{GetEngineConnAction, KillEngineConnAction}
import org.apache.linkis.computation.client.once.simple.SimpleOnceJob


trait OnceJob extends AbstractLinkisJob {

  protected val linkisManagerClient: LinkisManagerClient
  protected val user: String

  protected var engineConnId: String = _
  protected var serviceInstance: ServiceInstance = _
  protected var ticketId: String = _

  protected def wrapperEC[T](op: => T): T = wrapperObj(serviceInstance, "Please submit job first.")(op)

  protected override def doKill(): Unit = wrapperEC {
    linkisManagerClient.killEngineConn(KillEngineConnAction.newBuilder().setApplicationName(serviceInstance.getApplicationName)
      .setInstance(serviceInstance.getInstance).setUser(user).build())
  }

  def getNodeInfo: util.Map[String, Any] = wrapperEC {
    linkisManagerClient.getEngineConn(GetEngineConnAction.newBuilder().setApplicationName(serviceInstance.getApplicationName)
    .setInstance(serviceInstance.getInstance).setUser(user).build()).getNodeInfo
  }

  protected def getTicketId(nodeInfo: util.Map[String, Any]): String = getAs(nodeInfo, "ticketId")

  protected def getStatus(nodeInfo: util.Map[String, Any]): String = getAs(nodeInfo, "nodeStatus")

  protected def getServiceInstance(nodeInfo: util.Map[String, Any]): ServiceInstance = nodeInfo.get("serviceInstance") match {
    case serviceInstance: util.Map[String, Any] =>
      ServiceInstance(getAs(serviceInstance, "applicationName"), getAs(serviceInstance, "instance"))
  }

  protected def getECMServiceInstance(nodeInfo: util.Map[String, Any]): ServiceInstance = nodeInfo.get("ecmServiceInstance") match {
    case serviceInstance: util.Map[String, Any] =>
      ServiceInstance(getAs(serviceInstance, "applicationName"), getAs(serviceInstance, "instance"))
  }

  protected def getAs[T](map: util.Map[String, Any], key: String): T = map.get(key).asInstanceOf[T]

}

trait SubmittableOnceJob extends OnceJob with SubmittableLinkisJob

object OnceJob {
  val simple = SimpleOnceJob
}