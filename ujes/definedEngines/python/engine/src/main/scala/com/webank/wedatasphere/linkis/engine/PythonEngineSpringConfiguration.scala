/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine

import com.webank.wedatasphere.linkis.engine.execute.hook.{MaxExecuteNumEngineHook, ReleaseEngineHook}
import com.webank.wedatasphere.linkis.engine.execute.{CodeParser, EngineHook, PythonCodeParser}
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
  * created by allenlliu on 2018/12/14
  * Description:
  */
@Component
class PythonEngineSpringConfiguration {
  private val LOG = LoggerFactory.getLogger(getClass)

  @Bean(Array("codeParser"))
  def generateCodeParser:CodeParser = {
    LOG.info("code Parser is set in python")
    new PythonCodeParser()
  }

  @Bean(Array("engineHooks"))
  def generateEngineHooks:Array[EngineHook] = {
    LOG.info("engineHooks are set in python.")
    Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook)
  }
}
