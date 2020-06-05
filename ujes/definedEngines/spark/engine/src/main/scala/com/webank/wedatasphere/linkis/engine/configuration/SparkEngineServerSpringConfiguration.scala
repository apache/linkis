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

package com.webank.wedatasphere.linkis.engine.configuration

import com.webank.wedatasphere.linkis.engine.condition.EngineHooksCondition
import com.webank.wedatasphere.linkis.engine.execute.hook._
import com.webank.wedatasphere.linkis.engine.execute.{CodeParser, EngineHook, SparkCombinedCodeParser}
import org.springframework.context.annotation.{Bean, Conditional, Configuration}

/**
  * Created by allenlliu on 2018/12/3.
  */
@Configuration
class SparkEngineServerSpringConfiguration {
  @Bean(Array("codeParser"))
  def createCodeParser(): CodeParser = new SparkCombinedCodeParser()


  @Bean(Array("engineHooks"))
  @Conditional(Array(classOf[EngineHooksCondition]))
  def createEngineHooks(): Array[EngineHook] = Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook, new JarUdfEngineHook, new PyUdfEngineHook, new ScalaUdfEngineHook, new PyFunctionEngineHook, new ScalaFunctionEngineHook)
}
