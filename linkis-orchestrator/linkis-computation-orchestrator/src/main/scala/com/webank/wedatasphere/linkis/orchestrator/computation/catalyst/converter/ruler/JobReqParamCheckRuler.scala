package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext
import org.apache.commons.lang.StringUtils
/**
 *
 *
 */

class JobReqParamCheckRuler extends ConverterCheckRuler with Logging{


  override def apply(in: JobReq, context: ASTContext): Unit = {
    val executeUser = in.getExecuteUser
    val param = in.getParams
    if(StringUtils.isEmpty(executeUser)){
      throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE,
      s"job:${in.getName} execute user is null, please check request again!")
    }
  }

  override def getName: String = {
    val className = getClass.getName
    if (className endsWith "$") className.dropRight(1) else className
  }
}
