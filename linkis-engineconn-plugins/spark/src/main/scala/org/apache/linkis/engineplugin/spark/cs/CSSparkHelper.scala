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

import org.apache.linkis.cs.client.utils.ContextServiceUtils
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

import org.apache.spark.SparkContext

object CSSparkHelper {

  def setContextIDInfoToSparkConf(
      engineExecutorContext: EngineExecutionContext,
      sparkContext: SparkContext
  ): Unit = {
    val contextIDValueStr =
      ContextServiceUtils.getContextIDStrByMap(engineExecutorContext.getProperties)
    val nodeNameStr = ContextServiceUtils.getNodeNameStrByMap(engineExecutorContext.getProperties)
    sparkContext.setLocalProperty(CSCommonUtils.CONTEXT_ID_STR, contextIDValueStr)
    sparkContext.setLocalProperty(CSCommonUtils.NODE_NAME_STR, nodeNameStr)
  }

}
