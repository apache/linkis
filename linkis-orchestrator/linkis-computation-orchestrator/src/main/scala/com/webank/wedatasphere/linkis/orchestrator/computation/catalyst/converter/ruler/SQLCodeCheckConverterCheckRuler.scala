package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception.CodeCheckException
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext


class SQLCodeCheckConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = in match {
    case computationJobReq: ComputationJobReq =>
      computationJobReq.getCodeLanguageLabel.getCodeType.toLowerCase() match {
        case "hql" | "sql" | "jdbc"|"hive" | "psql" =>
          val sb:StringBuilder = new StringBuilder
          val isAuth:Boolean = SQLExplain.authPass(computationJobReq.getCodeLogicalUnit.toStringCode, sb)
          if (!isAuth) {
            throw CodeCheckException(20051, "sql code check failed, reason is " + sb.toString())
          }
        case _ =>
      }
  }

  override def getName: String = "SQLCodeCheckConverterCheckRuler"
}
