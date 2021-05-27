package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser.label

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.cache.CacheLabel
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.CodeJob
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils

class CacheParserLabelFiller extends ParserLabelFiller {

  override def parseToLabel(in: ASTOrchestration[_], context: ASTContext): Option[Label[_]] = {
    in match {
      case codeJob: CodeJob =>
        val runtimeMap = TaskUtils.getRuntimeMap(codeJob.getParams)
        val cache = runtimeMap.get(TaskConstant.CACHE)
        if(cache != null && cache.asInstanceOf[Boolean]){
          val cacheLabel = new CacheLabel
          cacheLabel.setCacheExpireAfter(runtimeMap.get(TaskConstant.CACHE_EXPIRE_AFTER).toString)
          cacheLabel.setReadCacheBefore(runtimeMap.get(TaskConstant.READ_CACHE_BEFORE).toString)
          Some(cacheLabel)
        } else {
          None
        }
      case _ => None

    }
  }

}
