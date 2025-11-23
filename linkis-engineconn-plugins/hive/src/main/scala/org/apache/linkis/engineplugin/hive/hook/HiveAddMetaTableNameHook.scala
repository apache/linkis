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

package org.apache.linkis.engineplugin.hive.hook

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.INVALID_VALUE
import org.apache.linkis.engineplugin.hive.exception.HiveQueryFailedException

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.hive.conf.HiveConf.ConfVars

import java.util
import java.util.regex.Pattern

import scala.collection.JavaConverters.mapAsScalaMapConverter

class HiveAddMetaTableNameHook extends ComputationExecutorHook with Logging {

  private val HIVE_USE_TABLENAME_REGEX =
    ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES.varname + "\\s*=\\s*(true|false)"

  override def getHookName(): String = "Add tableName config in metadata of hive result."

  override def beforeExecutorExecute(
      engineExecutionContext: EngineExecutionContext,
      engineCreationContext: EngineCreationContext,
      codeBeforeHook: String
  ): String = {
    val configMap = new util.HashMap[String, String]()
    engineCreationContext.getOptions.asScala
      .filterNot(_._2.isInstanceOf[util.Map[String, Any]])
      .foreach(kv => configMap.put(kv._1, s"${kv._2}"))
    engineExecutionContext.getProperties.asScala
      .filterNot(_._2.isInstanceOf[util.Map[String, Any]])
      .foreach(kv => configMap.put(kv._1, s"${kv._2}"))
    if (configMap.containsKey(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES.varname)) {
      engineExecutionContext.setEnableResultsetMetaWithTableName(
        configMap.get(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES.varname).toBoolean
      )
    }

    if (codeBeforeHook.contains(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES.varname)) {
      val pattern = Pattern.compile(HIVE_USE_TABLENAME_REGEX)
      codeBeforeHook
        .split("\n")
        .foreach(line => {
          if (StringUtils.isNotBlank(line)) {
            val mather = pattern.matcher(line)
            if (mather.find()) {
              val value = mather.group(1)
              Utils.tryCatch {
                val boolValue = value.toBoolean
                if (
                    engineExecutionContext.getProperties
                      .containsKey(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES.varname)
                ) {
                  logger.warn(
                    s"Should not add param ${mather.group()} in both code and starupMap, will use the param in code."
                  )
                }
                engineExecutionContext.setEnableResultsetMetaWithTableName(boolValue)
              } { case e: IllegalArgumentException =>
                throw HiveQueryFailedException(
                  INVALID_VALUE.getErrorCode,
                  INVALID_VALUE.getErrorDesc.concat(s" : ${value} in param [${mather.group()}]")
                )
              }
            }
          }
        })
    }
    codeBeforeHook
  }

}
