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

package org.apache.linkis.orchestrator.domain

import org.apache.linkis.manager.label.entity.Label

import java.util

/**
 */
trait JobReq extends Node {

  def getExecuteUser: String

  def getLabels: util.List[Label[_]]

  def getParams: java.util.Map[String, AnyRef]

  def getPriority: Int

}

abstract class AbstractJobReq extends JobReq {

  private var executeUser: String = _
  private var labels: util.List[Label[_]] = _
  private var params: util.Map[String, AnyRef] = _
  private var priority: Int = _

  override def getExecuteUser: String = executeUser

  override def getLabels: util.List[Label[_]] = labels

  override def getParams: util.Map[String, AnyRef] = params

  override def getPriority: Int = priority

}

object AbstractJobReq {

  abstract class AbstractJobReqBuilder {

    protected val jobReq: AbstractJobReq = createJobReq()

    def clone(jobReq: JobReq): AbstractJobReqBuilder = {
      setExecuteUser(jobReq.getExecuteUser)
      setLabels(jobReq.getLabels)
      setParams(jobReq.getParams)
      setPriority(jobReq.getPriority)
    }

    def setExecuteUser(executeUser: String): AbstractJobReqBuilder = {
      jobReq.executeUser = executeUser
      this
    }

    def setLabels(labels: util.List[Label[_]]): AbstractJobReqBuilder = {
      jobReq.labels = labels
      this
    }

    def setParams(params: util.Map[String, AnyRef]): AbstractJobReqBuilder = {
      jobReq.params = params
      this
    }

    def setPriority(priority: Int): AbstractJobReqBuilder = {
      jobReq.priority = priority
      this
    }

    def build(): JobReq = jobReq

    protected def createJobReq(): AbstractJobReq

  }

}

object JobReq {
  def getDefaultPriority: Int = 0
}
