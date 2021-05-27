package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext

class SQLLimitConverterCheckRuler  extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        computationJobReq.getCodeLanguageLabel.getCodeType.toLowerCase match {
          case "hql" | "sql" | "jdbc"|"hive" | "psql" =>
            val logicalUnit = computationJobReq.getCodeLogicalUnit.parseCodes({ c => SQLExplain.dealSQLLimit(c, new java.lang.StringBuilder()) })
            computationJobReq.setCodeLogicalUnit(logicalUnit)
          case _ =>
        }
      case _ =>
    }
  }

  override def getName: String = "SQLLimitConverterCheckRuler"
}
