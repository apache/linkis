/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
