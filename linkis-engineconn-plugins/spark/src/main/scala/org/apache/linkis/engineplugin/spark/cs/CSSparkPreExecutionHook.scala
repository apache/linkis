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

package org.apache.linkis.engineplugin.spark.cs

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.client.utils.ContextServiceUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.core.errorcode.LinkisEngineconnCoreErrorCodeSummary.CANNOT_PARSE_FOR_NODE
import org.apache.linkis.engineconn.core.exception.ExecutorHookFatalException
import org.apache.linkis.engineplugin.spark.extension.SparkPreExecutionHook

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import java.text.MessageFormat
import java.util

@Component
class CSSparkPreExecutionHook extends SparkPreExecutionHook with Logging {

  @PostConstruct
  def init(): Unit = {
    SparkPreExecutionHook.register(this)
  }

  override def hookName: String = "CSSparkPreExecutionHook"

  override def callPreExecutionHook(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): String = {

    var parsedCode = code
    val properties =
      if (engineExecutionContext != null) engineExecutionContext.getProperties
      else new util.HashMap[String, Object]()
    val contextIDValueStr =
      ContextServiceUtils.getContextIDStrByMap(properties)
    val nodeNameStr =
      ContextServiceUtils.getNodeNameStrByMap(properties)
    if (StringUtils.isBlank(contextIDValueStr) || StringUtils.isBlank(nodeNameStr)) {
      logger.info("cs info is null skip CSSparkPreExecutionHook")
      return code
    }
    logger.info(
      s"Start to call CSSparkPreExecutionHook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr"
    )

    parsedCode = Utils.tryCatch {
      CSTableParser.parse(engineExecutionContext, parsedCode, contextIDValueStr, nodeNameStr)
    } { case t: Throwable =>
      val msg = if (null != t) {
        t.getMessage
      } else {
        "null message"
      }
      logger.info("Failed to parser cs table because : ", msg)
      throw new ExecutorHookFatalException(
        CANNOT_PARSE_FOR_NODE.getErrorCode,
        MessageFormat.format(CANNOT_PARSE_FOR_NODE.getErrorDesc, nodeNameStr)
      )
    }
    logger.info(
      s"Finished to call CSSparkPreExecutionHook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr"
    )
    parsedCode
  }

}
