package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception.ScalaCodeCheckException
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext

class ScalaCodeConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = in match {
    case computationJobReq:ComputationJobReq =>
      val error = new StringBuilder
      computationJobReq.getCodeLanguageLabel.getCodeType match {
        case "scala" => Utils.tryThrow(ScalaExplain.authPass(computationJobReq.getCodeLogicalUnit.toStringCode, error)){
          case ScalaCodeCheckException(errorCode, errDesc) =>
            computationJobReq.setErrorCode(errorCode)
            computationJobReq.setErrorDesc(errDesc)
            ScalaCodeCheckException(errorCode, errDesc)
          case t:Throwable => val exception = ScalaCodeCheckException(20074, "Scala code check failed(scala代码检查失败)")
            exception.initCause(t)
            exception
        }
        case _ =>
      }
  }

  override def getName: String = "ScalaCodeConverterCheckRuler"
}
