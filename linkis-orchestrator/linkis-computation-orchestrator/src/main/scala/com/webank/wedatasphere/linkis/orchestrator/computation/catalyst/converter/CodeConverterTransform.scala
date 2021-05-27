package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.CodeJob
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}

/**
  * After the job of ConverterTransform, the logic unit in the job should be executable after compilation.
  */
class CodeConverterTransform extends ConverterTransform with Logging {

  override def apply(in: JobReq, context: ASTContext): Job = in match  {
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
    case _ => throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.CONVERTER_FOR_NOT_SUPPORT_ERROR_CODE,
      "CodeConverterTransform Cannot convert jobReq " + in)
  }



  override def getName: String = this.getClass.getSimpleName

}
