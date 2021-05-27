package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception.CodeCheckException
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext


class SparkCodeCheckConverterCheckRuler extends ConverterCheckRuler{

  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        computationJobReq.getCodeLanguageLabel.getCodeType.toLowerCase() match {
          case "scala" => val stringBuilder:StringBuilder = new StringBuilder()
            val isAuth = SparkExplain.authPass(computationJobReq.getCodeLogicalUnit.toStringCode, stringBuilder)
            if (!isAuth){
              throw CodeCheckException(20050, "spark code check failed")
            }
          case _ =>
        }
      case _ =>
    }
  }

  override def getName: String = "SparkCodeCheckConverterCheckRuler"
}
