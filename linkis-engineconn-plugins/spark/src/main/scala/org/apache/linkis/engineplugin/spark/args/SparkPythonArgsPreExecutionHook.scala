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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.common.SparkEnginePluginConst
import org.apache.linkis.engineplugin.spark.extension.SparkPreExecutionHook
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import java.util

/**
 * Set sys.argv[] if: 1. it is a pyspark task. 2. user provide with args in runtimeMap. 3. it is at
 * the beginning of the code
 */
@Component
class SparkPythonArgsPreExecutionHook extends SparkPreExecutionHook with Logging {

  @PostConstruct
  def init(): Unit = {
    SparkPreExecutionHook.register(this)
  }

  private val argsAppender = new SparkPythonArgsAppender

  override def hookName: String = "SparkPythonArgsPreExecutionHook"

  override def callPreExecutionHook(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): String = {

    val codeLanguageLabel = engineExecutionContext.getLabels
      .filter(l => null != l && l.isInstanceOf[CodeLanguageLabel])
      .head
    val runType: String = codeLanguageLabel match {
      case l: CodeLanguageLabel => l.getCodeType
      case _ => ""
    }

    if (
        engineExecutionContext.getTotalParagraph != 1 || StringUtils.isEmpty(
          runType
        ) || !StringUtils.equals(RunType.PYSPARK.toString, runType)
    ) {
      return code
    }

    val argsArr =
      if (
          engineExecutionContext.getProperties != null && engineExecutionContext.getProperties
            .containsKey(SparkEnginePluginConst.RUNTIME_ARGS_KEY)
      ) {
        Utils.tryCatch {
          val argsList = engineExecutionContext.getProperties
            .get(SparkEnginePluginConst.RUNTIME_ARGS_KEY)
            .asInstanceOf[util.ArrayList[String]]
          logger.info(
            "Will execute pyspark task with user-specified arguments: \'" + argsList
              .toArray(new Array[String](argsList.size()))
              .mkString("\' \'") + "\'"
          )
          argsList.toArray(new Array[String](argsList.size()))
        } { t =>
          logger.warn(
            "Cannot read user-input pyspark arguments. Will execute pyspark task without them.",
            t
          )
          null
        }
      } else {
        null
      }

    val parsedCode = if (argsArr != null && argsArr.length != 0) {
      val argvStrBuilder = new StringBuilder()
      argsArr.foreach(argv =>
        if (StringUtils.isNotBlank(argv)) argvStrBuilder.append("\'").append(argv).append("\' ")
      )
      logger.info(s"Start to append args. $argvStrBuilder")
      val result = Utils.tryCatch(argsAppender.appendArgvs(code, argsArr)) { t: Throwable =>
        logger.error(s"Failed to append args. $argvStrBuilder", t)
        code
      }
      logger.info(s"Finished to append args. $argvStrBuilder")
      result
    } else {
      code
    }

    parsedCode
  }

}
