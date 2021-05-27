package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser.label

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration}

trait ParserLabelFiller {

  def parseToLabel(in: ASTOrchestration[_], context: ASTContext) : Option[Label[_]]

}
