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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser

import scala.collection.JavaConverters._

import org.slf4j.LoggerFactory

class HiveExecutionContextPropertyPrepareHook extends ComputationExecutorHook with Logging {

  private val LOG = LoggerFactory.getLogger(getClass)

  override def getHookName(): String = "Prepare HiveExecutionContext Property."

  override def beforeExecutorExecute(
      engineExecutionContext: EngineExecutionContext,
      engineCreationContext: EngineCreationContext,
      codeBeforeHook: String
  ): String = {
    LOG.info(s"execute HiveExecutionContextInitHook beforeExecutorExecute")
    val labels = engineCreationContext.getLabels.asScala
      .map(l => EngineConnArgumentsParser.LABEL_PREFIX + l.getLabelKey -> l.getStringValue)
      .toMap
    var engineConnConf =
      Map("ticketId" -> engineCreationContext.getTicketId, "user" -> engineCreationContext.getUser)
    engineConnConf = engineConnConf ++: labels
    engineConnConf = engineConnConf ++: engineCreationContext.getOptions.asScala
      .filterNot(_._1.startsWith("spring."))
      .toMap

    for (kv <- engineConnConf) {
      LOG.info(
        s"beforeExecutorExecute engineExecutionContext addProperty key: ${kv._1} value: ${kv._2}"
      )
      engineExecutionContext.addProperty(kv._1, kv._2)
    }

    codeBeforeHook
  }

}
