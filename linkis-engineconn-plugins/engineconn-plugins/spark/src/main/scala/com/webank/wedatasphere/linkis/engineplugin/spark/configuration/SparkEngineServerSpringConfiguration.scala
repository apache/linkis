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
package com.webank.wedatasphere.linkis.engineplugin.spark.configuration

import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.parser.CodeParser
import com.webank.wedatasphere.linkis.engineplugin.spark.executor.parser.SparkCombinedCodeParser
import com.webank.wedatasphere.linkis.engineplugin.spark.hook.{SparkPythonVersionEngineHook, UserDataBaseHook}
import org.springframework.context.annotation.{Bean, Configuration}

/**
  *
  */
@Configuration
class SparkEngineServerSpringConfiguration {
  @Bean(Array("codeParser"))
  def createCodeParser(): CodeParser = new SparkCombinedCodeParser()


  @Bean(Array("engineHooks"))
//  def createEngineHooks(): Array[EngineConnHook] = Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook,new SparkPythonVersionEngineHook, new JarUdfEngineHook, new PyUdfEngineHook, new ScalaUdfEngineHook, new PyFunctionEngineHook, new ScalaFunctionEngineHook,new UserDataBaseHook)
  def createEngineHooks(): Array[EngineConnHook] = Array(new SparkPythonVersionEngineHook, new UserDataBaseHook)
  // todo check
}
