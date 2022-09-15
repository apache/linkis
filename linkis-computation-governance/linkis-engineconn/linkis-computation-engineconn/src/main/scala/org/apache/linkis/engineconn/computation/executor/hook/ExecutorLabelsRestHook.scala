/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.computation.executor.hook

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}
import org.apache.linkis.manager.label.utils.LabelUtil

trait ExecutorLabelsRestHook {
  def getOrder(): Int = 1

  def getHookName(): String

  def restExecutorLabels(labels: Array[Label[_]]): Array[Label[_]]
}

class CreatorRestLabelsHook extends ExecutorLabelsRestHook with Logging {

  override def getHookName(): String = "CreatorRestLabelsHook"

  override def restExecutorLabels(labels: Array[Label[_]]): Array[Label[_]] = {
    val codeLabel = LabelUtil.getLabelFromArray[CodeLanguageLabel](labels)
    if (
        null != codeLabel && RunType.FUNCTION_MDQ_TYPE.toString.equalsIgnoreCase(
          codeLabel.getCodeType
        )
    ) {
      logger.info(s"code Type Label($codeLabel) reset to scala")
      val newCodeLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[CodeLanguageLabel])
      newCodeLabel.setCodeType(RunType.SCALA.toString)
      labels.filter(!_.isInstanceOf[CodeLanguageLabel]) ++ Array(newCodeLabel)
    } else {
      labels
    }
  }

}

object ExecutorLabelsRestHook {

  private val HOOKS = Array[ExecutorLabelsRestHook](new CreatorRestLabelsHook)

  def getExecutorLabelsRestHooks: Array[ExecutorLabelsRestHook] = HOOKS

}
