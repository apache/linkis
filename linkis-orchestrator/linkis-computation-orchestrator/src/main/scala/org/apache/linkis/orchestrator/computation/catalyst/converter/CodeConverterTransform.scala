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

package org.apache.linkis.orchestrator.computation.catalyst.converter

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.code.plans.ast.CodeJob
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException
}
import org.apache.linkis.orchestrator.extensions.catalyst.ConverterTransform
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, Job}

/**
 * After the job of ConverterTransform, the logic unit in the job should be executable after
 * compilation.
 */
class CodeConverterTransform extends ConverterTransform with Logging {

  override def apply(in: JobReq, context: ASTContext): Job = in match {
    case computationJobReq: ComputationJobReq =>
      val codeJob = new CodeJob(null, null)
      codeJob.setAstContext(context)
      codeJob.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit)
      codeJob.setParams(computationJobReq.getParams)
      codeJob.setName(computationJobReq.getName + "_Job")
      codeJob.setSubmitUser(computationJobReq.getSubmitUser)
      codeJob.setExecuteUser(computationJobReq.getExecuteUser)
      codeJob.setLabels(computationJobReq.getLabels)
      codeJob.setPriority(computationJobReq.getPriority)
      codeJob
    case _ =>
      throw new OrchestratorErrorException(
        OrchestratorErrorCodeSummary.CONVERTER_FOR_NOT_SUPPORT_ERROR_CODE,
        "CodeConverterTransform Cannot convert jobReq " + in
      )
  }

  override def getName: String = this.getClass.getSimpleName

}
