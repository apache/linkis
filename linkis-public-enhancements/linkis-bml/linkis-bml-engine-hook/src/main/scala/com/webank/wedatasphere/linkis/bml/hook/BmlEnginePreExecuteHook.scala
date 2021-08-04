/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.bml.hook

import java.io.File
import java.util
import com.webank.wedatasphere.linkis.bml.client.{BmlClient, BmlClientFactory}
import com.webank.wedatasphere.linkis.bml.exception.BmlHookDownloadException
import com.webank.wedatasphere.linkis.bml.utils.BmlHookUtils
import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.utlis.ComputationEngineConstant
import com.webank.wedatasphere.linkis.engineconn.core.util.EngineConnUtils
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions.asScalaBuffer

class BmlEnginePreExecuteHook extends ComputationExecutorHook with Logging{
  override def getHookName: String = "BmlEnginePreExecuteHook"

  override def getOrder(): Int = ComputationEngineConstant.CS_HOOK_ORDER + 1

  val processUser:String = System.getProperty("user.name")

  val defaultUser:String = "hadoop"

  val bmlClient:BmlClient = if (StringUtils.isNotEmpty(processUser))
    BmlClientFactory.createBmlClient(processUser) else BmlClientFactory.createBmlClient(defaultUser)

  val seperator:String = File.separator

  val pathType:String = "file://"


  override def beforeExecutorExecute(engineExecutionContext: EngineExecutionContext, engineCreationContext: EngineCreationContext, code: String): String = {
    val props = engineExecutionContext.getProperties
    if (null != props && props.containsKey(GovernanceConstant.TASK_RESOURCES_STR)) {
      val workDir = BmlHookUtils.getCurrentWorkDir
      val jobId = engineExecutionContext.getJobId
      props.get(GovernanceConstant.TASK_RESOURCES_STR) match {
        case resources: util.List[Object] =>
          resources.foreach {
            case resource: util.Map[String, Object] => val fileName = resource.get(GovernanceConstant.TASK_RESOURCE_FILE_NAME_STR).toString
              val resourceId = resource.get(GovernanceConstant.TASK_RESOURCE_ID_STR).toString
              val version = resource.get(GovernanceConstant.TASK_RESOURCE_VERSION_STR).toString
              val fullPath = if (workDir.endsWith(seperator)) pathType + workDir + fileName else
                pathType + workDir + seperator + fileName
              val response = Utils.tryCatch {
                bmlClient.downloadShareResource(processUser, resourceId, version, fullPath, true)
              } {
                case error: ErrorException => logger.error(s"download resource for $jobId failed", error)
                  throw error
                case t: Throwable => logger.error(s"download resource for $jobId failed", t)
                  val e1 = BmlHookDownloadException(t.getMessage)
                  e1.initCause(t)
                  throw t
              }
              if (response.isSuccess) {
                logger.info(s"for job $jobId resourceId $resourceId version $version download to path $fullPath ok")
              } else {
                logger.warn(s"for job $jobId resourceId $resourceId version $version download to path $fullPath Failed")
              }
            case _ => logger.warn("job resource cannot download")
          }
        case o =>
          info(s"Invalid resources : ${EngineConnUtils.GSON.toJson(o)}")
      }
    }
    code
  }
}
