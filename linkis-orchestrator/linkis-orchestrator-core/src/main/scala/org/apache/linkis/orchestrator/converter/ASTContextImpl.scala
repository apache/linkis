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

package org.apache.linkis.orchestrator.converter

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.plans.SimplifyPlanContext
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, QueryParams, QueryParamsImpl}

import java.util

/**
 */
class ASTContextImpl extends SimplifyPlanContext with ASTContext {

  private var labels: util.List[Label[_]] = _
  private var params: QueryParams = _
  private var priority: Int = _
  private var executeUser: String = _

  override def getLabels: util.List[Label[_]] = labels

  override def getParams: QueryParams = params

  override def getPriority: Int = priority

  override def getExecuteUser: String = executeUser

}

object ASTContextImpl {

  def newBuilder(): Builder = new Builder

  class Builder {

    private val astContext = new ASTContextImpl

    def setExecuteUser(executeUser: String): Builder = {
      astContext.executeUser = executeUser
      this
    }

    def setPriority(priority: Int): Builder = {
      astContext.priority = priority
      this
    }

    def setParams(params: java.util.Map[String, AnyRef]): Builder = {

      astContext.params = new QueryParamsImpl(params)

      this
    }

    def setLabels(labels: util.List[Label[_]]): Builder = {
      astContext.labels = labels
      this
    }

    def setJobReq(jobReq: JobReq): Builder = setExecuteUser(jobReq.getExecuteUser)
      .setLabels(jobReq.getLabels)
      .setParams(jobReq.getParams)
      .setPriority(jobReq.getPriority)

    def build(): ASTContext = astContext

  }

}
