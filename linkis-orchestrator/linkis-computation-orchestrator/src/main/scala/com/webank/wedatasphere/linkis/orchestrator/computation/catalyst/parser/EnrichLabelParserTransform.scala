package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser

import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.CodeJob
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser.label.{CacheParserLabelFiller, ParserLabelFiller}
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ParserTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}

class EnrichLabelParserTransform extends ParserTransform {

  var parserLabelFillers: Array[ParserLabelFiller] = _

  initParserLabelFillers

  override def apply(in: Job, context: ASTContext): Job = {
    in match {
      case codeJob: CodeJob =>
        enrichLabels(codeJob, context)
        codeJob
      case _ => throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.PARSER_FOR_NOT_SUPPORT_ERROR_CODE,
        "EnrichLabelParserTransform Cannot convert job " + in)
    }
  }

  def enrichLabels(codeJob: CodeJob, context: ASTContext) = {
    parserLabelFillers.foreach{ filler =>
      filler.parseToLabel(codeJob, context) match {
        case Some(label) => context.getLabels.add(label)
        case None =>
      }
    }
  }

  def initParserLabelFillers = {
    parserLabelFillers = Array(new CacheParserLabelFiller)
  }

  override def getName: String = "EnrichLabelParserTransform"

}
