package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception.PythonCodeCheckException
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext
import org.apache.commons.lang.exception.ExceptionUtils

class PythonCodeConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = in match {
    case computationJobReq: ComputationJobReq =>
      val error = new StringBuilder
      computationJobReq.getCodeLanguageLabel.getCodeType match {
        case "python" | "pyspark" | "py" =>
          Utils.tryThrow(PythonExplain.authPass(computationJobReq.getCodeLogicalUnit.toStringCode, error)){
            case PythonCodeCheckException(errCode,errDesc) =>
              computationJobReq.setErrorCode(errCode)
              computationJobReq.setErrorDesc(errDesc)
              PythonCodeCheckException(errCode, errDesc)
            case t: Throwable =>
              val exception = PythonCodeCheckException(20073, "Checking python code failed!(检查python代码失败！)" + ExceptionUtils.getRootCauseMessage(t))
              exception.initCause(t)
              exception
          }
        case _ =>
      }
  }

  override def getName: String = "PythonCodeConverterCheckRuler"
}
