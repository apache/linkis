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

package org.apache.linkis.engineconn.computation.executor.bml

import org.apache.linkis.bml.client.{BmlClient, BmlClientFactory}
import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.computation.executor.utlis.{
  ComputationEngineConstant,
  ComputationEngineUtils
}
import org.apache.linkis.engineconn.core.util.EngineConnUtils
import org.apache.linkis.governance.common.utils.GovernanceConstant
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.util

import scala.collection.JavaConverters._

class BmlEnginePreExecuteHook extends ComputationExecutorHook with Logging {
  override def getHookName: String = "BmlEnginePreExecuteHook"

  override def getOrder(): Int = ComputationEngineConstant.CS_HOOK_ORDER + 1

  private val processUser: String = System.getProperty("user.name")

  private val defaultUser: String = "hadoop"

  private val bmlClient: BmlClient =
    if (StringUtils.isNotEmpty(processUser)) BmlClientFactory.createBmlClient(processUser)
    else BmlClientFactory.createBmlClient(defaultUser)

  private val seperator: String = File.separator

  private val pathType: String = StorageUtils.FILE_SCHEMA

  override def beforeExecutorExecute(
      engineExecutionContext: EngineExecutionContext,
      engineCreationContext: EngineCreationContext,
      code: String
  ): String = {
    val props = engineExecutionContext.getProperties
    if (null != props && props.containsKey(GovernanceConstant.TASK_RESOURCES_STR)) {
      val jobId = engineExecutionContext.getJobId
      props.get(GovernanceConstant.TASK_RESOURCES_STR) match {
        case resources: util.List[Object] =>
          resources.asScala.foreach {
            case resource: util.Map[String, Object] =>
              val fileName = resource.get(GovernanceConstant.TASK_RESOURCE_FILE_NAME_STR).toString
              val resourceId = resource.get(GovernanceConstant.TASK_RESOURCE_ID_STR).toString
              val version = resource.get(GovernanceConstant.TASK_RESOURCE_VERSION_STR).toString
              val fullPath = fileName
              val response = Utils.tryCatch {
                bmlClient.downloadShareResource(processUser, resourceId, version, fullPath, true)
              } {
                case error: ErrorException =>
                  logger.error(s"download resource for $jobId failed", error)
                  throw error
                case t: Throwable =>
                  logger.error(s"download resource for $jobId failed", t)
                  val e1 = BmlHookDownloadException(t.getMessage)
                  e1.initCause(t)
                  throw t
              }
              if (response.isSuccess) {
                logger.info(
                  s"for job $jobId resourceId $resourceId version $version download to path $fullPath ok"
                )
              } else {
                logger.warn(
                  s"for job $jobId resourceId $resourceId version $version download to path $fullPath Failed"
                )
              }
            case _ => logger.warn("job resource cannot download")
          }
        case o =>
          logger.info(s"Invalid resources : ${EngineConnUtils.GSON.toJson(o)}")
      }
    }
    code
  }

}
