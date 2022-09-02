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

package org.apache.linkis.engineconn.common.creation

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.label.entity.Label

import java.util

trait EngineCreationContext {

  def getUser: String

  def setUser(user: String): Unit

  def getTicketId: String

  def setTicketId(ticketId: String): Unit

  def getLabels(): util.List[Label[_]]

  def setLabels(labels: util.List[Label[_]]): Unit

  def getEMInstance: ServiceInstance

  def setEMInstance(instance: ServiceInstance): Unit

  def getOptions: util.Map[String, String]

  def setOptions(options: util.Map[String, String])

  def getExecutorId: Int

  def setExecutorId(id: Int)

  def setArgs(args: Array[String])

  def getArgs: Array[String]
}

class DefaultEngineCreationContext extends EngineCreationContext {

  private var user: String = _

  private var ticketId: String = _

  private var labels: util.List[Label[_]] = _

  private var options: util.Map[String, String] = _

  private var emInstance: ServiceInstance = _

  private var executorId: Int = 0

  private var args: Array[String] = null

  override def getTicketId: String = this.ticketId

  override def setTicketId(ticketId: String): Unit = this.ticketId = ticketId

  override def getLabels(): util.List[Label[_]] = this.labels

  override def setLabels(labels: util.List[Label[_]]): Unit = this.labels = labels

  override def getEMInstance: ServiceInstance = this.emInstance

  override def setEMInstance(instance: ServiceInstance): Unit = this.emInstance = instance

  override def getOptions: util.Map[String, String] = this.options

  override def setOptions(options: util.Map[String, String]): Unit = this.options = options

  override def getUser: String = user

  override def setUser(user: String): Unit = this.user = user

  override def getExecutorId: Int = executorId

  override def setExecutorId(id: Int): Unit = executorId = id

  override def setArgs(args: Array[String]): Unit = this.args = args

  override def getArgs: Array[String] = args

  override def toString: String =
    s"DefaultEngineCreationContext(user-$user, ticketID-$ticketId, emInstance-$emInstance)"

}
