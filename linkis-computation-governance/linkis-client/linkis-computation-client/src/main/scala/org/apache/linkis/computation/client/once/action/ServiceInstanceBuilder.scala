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

package org.apache.linkis.computation.client.once.action

abstract class ServiceInstanceBuilder[T <: GetEngineConnAction] private[action] () {
  private var applicationName: String = _
  private var instance: String = _
  private var user: String = _
  private var ticketId: String = _

  def setApplicationName(applicationName: String): this.type = {
    this.applicationName = applicationName
    this
  }

  def setInstance(instance: String): this.type = {
    this.instance = instance
    this
  }

  def setUser(user: String): this.type = {
    this.user = user
    this
  }

  def setTicketId(ticketId: String): this.type = {
    this.ticketId = ticketId
    this
  }

  protected def createGetEngineConnAction(): T

  def build(): T = {
    val getEngineConnAction = createGetEngineConnAction()
    getEngineConnAction.setUser(user)
    getEngineConnAction.addRequestPayload("applicationName", applicationName)
    getEngineConnAction.addRequestPayload("instance", instance)
    getEngineConnAction.addRequestPayload("ticketId", ticketId)
    getEngineConnAction
  }

}
