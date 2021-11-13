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
 
package org.apache.linkis.computation.client.operator

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.computation.client.once.LinkisManagerClient
import org.apache.linkis.computation.client.once.action.EngineConnOperateAction
import org.apache.linkis.computation.client.once.result.EngineConnOperateResult


trait OnceJobOperator[T] extends Operator[T] with Logging {

  private var user: String = _
  private var serviceInstance: ServiceInstance = _
  private var linkisManagerClient: LinkisManagerClient = _

  protected def getUser: String = user
  protected def getServiceInstance: ServiceInstance = serviceInstance
  protected def getLinkisManagerClient: LinkisManagerClient = linkisManagerClient

  def setUser(user: String): this.type = {
    this.user = user
    this
  }

  def setServiceInstance(serviceInstance: ServiceInstance): this.type = {
    this.serviceInstance = serviceInstance
    this
  }

  def setLinkisManagerClient(linkisManagerClient: LinkisManagerClient): this.type = {
    this.linkisManagerClient = linkisManagerClient
    this
  }

  override def apply(): T = {
    val builder = EngineConnOperateAction.newBuilder()
      .operatorName(getName)
      .setUser(user)
      .setApplicationName(serviceInstance.getApplicationName)
      .setInstance(serviceInstance.getInstance)
    addParameters(builder)
    val engineConnOperateAction = builder.build()
    info(s"$getUser try to ask EngineConn($serviceInstance) to execute $getName operation, parameters is ${engineConnOperateAction.getRequestPayload}.")
    val result = linkisManagerClient.executeEngineConnOperation(engineConnOperateAction)
    info(s"$getUser asked EngineConn($serviceInstance) to execute $getName operation, results is ${result.getResult}.")
    resultToObject(result)
  }

  protected def addParameters(builder: EngineConnOperateAction.Builder): Unit = {}

  protected def resultToObject(result: EngineConnOperateResult): T

}
