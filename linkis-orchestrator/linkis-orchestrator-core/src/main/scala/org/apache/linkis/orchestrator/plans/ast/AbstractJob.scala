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

package org.apache.linkis.orchestrator.plans.ast

import org.apache.linkis.manager.label.entity.Label

import java.util

abstract class AbstractJob extends Job {

  private var labels: util.List[Label[_]] = _

  private var params: util.Map[String, AnyRef] = _

  private var priority: Int = _

  private var executeUser: String = _

  def getParams: util.Map[String, AnyRef] = params

  def setParams(params: util.Map[String, AnyRef]): Unit = this.params = params

  def getExecuteUser: String = executeUser

  def setExecuteUser(executeUser: String): Unit = this.executeUser = executeUser

  def getLabels: util.List[Label[_]] = labels

  def setLabels(labels: util.List[Label[_]]): Unit = this.labels = labels

  def getPriority: Int = priority

  def setPriority(priority: Int): Unit = this.priority = priority

}
