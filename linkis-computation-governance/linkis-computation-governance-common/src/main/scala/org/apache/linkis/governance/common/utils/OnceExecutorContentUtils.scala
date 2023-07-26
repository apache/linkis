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

package org.apache.linkis.governance.common.utils

import org.apache.linkis.governance.common.entity.job.OnceExecutorContent
import org.apache.linkis.governance.common.exception.GovernanceErrorException
import org.apache.linkis.protocol.constants.TaskConstant

import java.util

object OnceExecutorContentUtils {

  val ONCE_EXECUTOR_CONTENT_KEY = "onceExecutorContent"
  private val HEADER = "resource_"
  private val LEN = 3

  def resourceToValue(bmlResource: BmlResource): String = {
    val resourceId = bmlResource.getResourceId
    val resourceIdLength = resourceId.length.toString.length
    if (resourceIdLength > LEN) {
      throw new GovernanceErrorException(
        40108,
        s"Invalid resourceId $resourceId, it is too length."
      )
    }
    val len = if (resourceIdLength < LEN) "0" * (LEN - resourceIdLength) + resourceId.length
    HEADER + len + resourceId + bmlResource.getVersion
  }

  def valueToResource(resource: String): BmlResource = {
    if (!resource.startsWith(HEADER)) {
      throw new GovernanceErrorException(
        40108,
        s"Invalid resource $resource, it doesn't contain $HEADER."
      )
    }
    val value = resource.substring(HEADER.length)
    val len = value.substring(0, LEN)
    val resourceId = value.substring(LEN, len.toInt + LEN)
    val version = value.substring(len.toInt + LEN)
    BmlResource(resourceId, version)
  }

  def mapToContent(contentMap: util.Map[String, Object]): OnceExecutorContent = {
    val onceExecutorContent = new OnceExecutorContent
    def getOrNull(key: String): util.Map[String, Object] = contentMap.get(key) match {
      case map: util.Map[String, Object] => map
      case _ => null
    }
    onceExecutorContent.setJobContent(getOrNull(TaskConstant.JOB_CONTENT))
    onceExecutorContent.setRuntimeMap(getOrNull(TaskConstant.PARAMS_CONFIGURATION_RUNTIME))
    onceExecutorContent.setSourceMap(getOrNull(TaskConstant.SOURCE))
    onceExecutorContent.setVariableMap(getOrNull(TaskConstant.PARAMS_VARIABLE))
    onceExecutorContent
  }

  def contentToMap(onceExecutorContent: OnceExecutorContent): util.Map[String, Object] = {
    val contentMap = new util.HashMap[String, Object]()
    contentMap.put(TaskConstant.JOB_CONTENT, onceExecutorContent.getJobContent)
    contentMap.put(TaskConstant.PARAMS_CONFIGURATION_RUNTIME, onceExecutorContent.getRuntimeMap)
    contentMap.put(TaskConstant.SOURCE, onceExecutorContent.getSourceMap)
    contentMap.put(TaskConstant.PARAMS_VARIABLE, onceExecutorContent.getVariableMap)
    contentMap
  }

  trait BmlResource {
    def getResourceId: String
    def getVersion: String
    override def toString: String = s"BmlResource($getResourceId, $getVersion)"
  }

  object BmlResource {

    def apply(resourceId: String, version: String): BmlResource = new BmlResource {
      override def getResourceId: String = resourceId
      override def getVersion: String = version
    }

  }

}
