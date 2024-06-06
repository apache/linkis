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

package org.apache.linkis.computation.client.once.result

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult

import java.util

@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/getEngineConn")
class GetEngineConnResult extends LinkisManagerResult {

  private var engineConnNode: util.Map[String, Any] = _

  def setEngine(engine: util.Map[String, Any]): Unit = {
    this.engineConnNode = engine
  }

  def getNodeInfo: util.Map[String, Any] = engineConnNode

  protected def getAs[T](map: util.Map[String, Any], key: String): T =
    map.get(key).asInstanceOf[T]

  def getTicketId(): String = getAs(engineConnNode, "ticketId")

  def getServiceInstance(): ServiceInstance =
    engineConnNode.get("serviceInstance") match {
      case serviceInstance: util.Map[String, Any] =>
        ServiceInstance(
          getAs(serviceInstance, "applicationName"),
          getAs(serviceInstance, "instance")
        )
      case _ => null
    }

  def getNodeStatus(): String = getAs(engineConnNode, "nodeStatus")

  def getECMServiceInstance(): ServiceInstance =
    engineConnNode.get("ecmServiceInstance") match {
      case serviceInstance: util.Map[String, Any] =>
        ServiceInstance(
          getAs(serviceInstance, "applicationName"),
          getAs(serviceInstance, "instance")
        )
      case _ => null
    }

  def getManagerServiceInstance(): ServiceInstance =
    engineConnNode.get("managerServiceInstance") match {
      case serviceInstance: util.Map[String, Any] =>
        ServiceInstance(
          getAs(serviceInstance, "applicationName"),
          getAs(serviceInstance, "instance")
        )
      case _ => null
    }

}
