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

package org.apache.linkis.engineplugin.spark.args

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.common.SparkKind
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.extension.SparkPreExecutionHook
import org.apache.linkis.manager.label.utils.LabelUtil

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import scala.collection.JavaConverters.seqAsJavaListConverter

@Component
class SparkScalaPreExecutionHook extends SparkPreExecutionHook with Logging {

  @PostConstruct
  private def init(): Unit = {
    SparkPreExecutionHook.register(this)
  }

  override def hookName: String = getClass.getSimpleName

  override def callPreExecutionHook(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): String = {
    if (!SparkConfiguration.ENABLE_REPLACE_PACKAGE_NAME.getValue || StringUtils.isBlank(code)) {
      return code
    }
    val codeTypeLabel = LabelUtil.getCodeTypeLabel(engineExecutionContext.getLabels.toList.asJava)
    if (null != codeTypeLabel) {
      codeTypeLabel.getCodeType.toLowerCase() match {
        case SparkKind.SCALA_LAN | SparkKind.FUNCTION_MDQ_TYPE =>
          if (!code.contains(SparkConfiguration.REPLACE_PACKAGE_HEADER.getValue)) {
            return code
          } else {
            logger.info(
              s"Replaced ${SparkConfiguration.REPLACE_PACKAGE_HEADER.getValue} to ${SparkConfiguration.REPLACE_PACKAGE_TO_HEADER}"
            )
            return code.replaceAll(
              SparkConfiguration.REPLACE_PACKAGE_HEADER.getValue,
              SparkConfiguration.REPLACE_PACKAGE_TO_HEADER
            )
          }
        case _ =>
      }
    }
    code
  }

}
