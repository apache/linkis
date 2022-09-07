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

package org.apache.linkis.orchestrator.computation.entity

import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.orchestrator.domain.{AbstractJobReq, JobReq}
import org.apache.linkis.orchestrator.domain.AbstractJobReq.AbstractJobReqBuilder
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit

/**
 */
class ComputationJobReq extends AbstractJobReq {

  private var id: String = _

  private var submitUser: String = _

  private var codeLogicalUnit: CodeLogicalUnit = _

  private var errorCode: Int = _

  private var errorDesc: String = _

  def getSubmitUser: String = submitUser

  override def getId: String = id

  def setId(id: String): Unit = this.id = id

  override def getName: String = s"ComputationJobReq_$id"

  def getCodeLogicalUnit: CodeLogicalUnit = codeLogicalUnit

  def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): Unit = this.codeLogicalUnit =
    codeLogicalUnit

  def getCodeLanguageLabel: CodeLanguageLabel = {
    codeLogicalUnit.getLabel
  }

  def getErrorCode: Int = errorCode

  def setErrorCode(errorCode: Int): Unit = this.errorCode = errorCode

  def getErrorDesc: String = errorDesc

  def setErrorDesc(errorDesc: String): Unit = this.errorDesc = errorDesc

}

object ComputationJobReq {
  def newBuilder(): ComputationJobReqBuilder = new ComputationJobReqBuilder

  class ComputationJobReqBuilder extends AbstractJobReqBuilder {

    def setId(id: String): ComputationJobReqBuilder = {
      jobReq match {
        case computationJobReq: ComputationJobReq => computationJobReq.id = id
        case _ =>
      }
      this
    }

    override def clone(jobReq: JobReq): AbstractJobReqBuilder = {
      super.clone(jobReq)
      jobReq match {
        case computationJobReq: ComputationJobReq =>
          setSubmitUser(computationJobReq.getSubmitUser)
          setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit)
        case _ =>
      }
      this
    }

    def setSubmitUser(submitUser: String): ComputationJobReqBuilder = {
      jobReq match {
        case computationJobReq: ComputationJobReq => computationJobReq.submitUser = submitUser
        case _ =>
      }
      this
    }

    def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): ComputationJobReqBuilder = {
      jobReq match {
        case computationJobReq: ComputationJobReq =>
          computationJobReq.codeLogicalUnit = codeLogicalUnit
        case _ =>
      }
      this
    }

    override protected def createJobReq(): AbstractJobReq = {
      new ComputationJobReq
    }

  }

}
