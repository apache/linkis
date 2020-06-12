/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.cs

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.client.service.{CSNodeServiceImpl, CSVariableService, LinkisJobDataServiceImpl}
import com.webank.wedatasphere.linkis.cs.client.utils.{ContextServiceUtils, SerializeHelper}
import com.webank.wedatasphere.linkis.cs.common.entity.`object`.LinkisVariable
import com.webank.wedatasphere.linkis.cs.common.entity.data.LinkisJobData
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.{ContextScope, ContextType}
import com.webank.wedatasphere.linkis.cs.common.entity.source.{CommonContextKey, LinkisWorkflowContextID}
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * @author peacewong
  * @date 2020/3/5 15:33
  */
object CSEntranceHelper extends Logging {


  def getContextInfo(params: util.Map[String, Any]): (String, String) = {

    val runtimeMap = params.get(TaskConstant.PARAMS_CONFIGURATION) match {
      case map: util.Map[String, AnyRef] => map.get(TaskConstant.PARAMS_CONFIGURATION_RUNTIME)
      case _ => null
    }

    if (null != runtimeMap) {
      runtimeMap match {
        case map: util.Map[String, AnyRef] =>
          val name = ContextServiceUtils.getNodeNameStrByMap(map)
          return (ContextServiceUtils.getContextIDStrByMap(map), name)
        case _ =>
      }
    }
    (null, null)
  }

  def setContextInfo(params: util.Map[String, Any], copyMap: util.Map[String, String]): Unit = {
    val (contextIDValueStr, nodeNameStr) = getContextInfo(params)
    if (StringUtils.isNotBlank(contextIDValueStr)) {
      copyMap.put(CSCommonUtils.CONTEXT_ID_STR, contextIDValueStr)
      copyMap.put(CSCommonUtils.NODE_NAME_STR, nodeNameStr)
    }
  }


  /**
    * register job id to cs
    *
    * @param job
    */
  def registerCSRSData(job: Job): Unit = {
    job match {
      case entranceJob: EntranceJob => {
        val (contextIDValueStr, nodeNameStr) = getContextInfo(entranceJob.getParams)
        info(s"registerCSRSData: nodeName:$nodeNameStr")
        if (StringUtils.isBlank(contextIDValueStr) || StringUtils.isBlank(nodeNameStr)) return null

        val contextKey = new CommonContextKey
        contextKey.setContextScope(ContextScope.PUBLIC)
        contextKey.setContextType(ContextType.DATA)
        contextKey.setKey(CSCommonUtils.NODE_PREFIX + nodeNameStr + CSCommonUtils.JOB_ID)
        entranceJob.getTask match {
          case requestPersistTask: RequestPersistTask =>
            val data = new LinkisJobData
            data.setJobID(requestPersistTask.getTaskID)
            LinkisJobDataServiceImpl.getInstance().putLinkisJobData(contextIDValueStr, SerializeHelper.serializeContextKey(contextKey), data)
            info(s"(${contextKey.getKey} put ${requestPersistTask.getTaskID} of taskID  to cs)")
          case _ =>
        }
        info(s"registerCSRSData end: nodeName:$nodeNameStr")
      }
      case _ =>
    }
  }

  /**
    * initNodeCSInfo
    *
    * @param requestPersistTask
    * @return
    */
  def initNodeCSInfo(requestPersistTask: RequestPersistTask): Unit = {

    val (contextIDValueStr, nodeNameStr) = getContextInfo(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])

    if (StringUtils.isNotBlank(contextIDValueStr) && StringUtils.isNotBlank(nodeNameStr)) {
      info(s"init node($nodeNameStr) cs info")
      CSNodeServiceImpl.getInstance().initNodeCSInfo(contextIDValueStr, nodeNameStr)
    }
  }


  /**
    * reset creator by contextID information
    * 1. Not set If contextID does not exists
    * 2. If env of contextID are dev set  nodeexecution
    * 3. If env of contextID are prod set scheduler
    *
    * @param requestPersistTask
    */
  def resetCreator(requestPersistTask: RequestPersistTask): Unit = {

    val (contextIDValueStr, nodeNameStr) = getContextInfo(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])

    if (StringUtils.isNotBlank(contextIDValueStr) && StringUtils.isNotBlank(nodeNameStr)) {
      SerializeHelper.deserializeContextID(contextIDValueStr) match {
        case contextID: LinkisWorkflowContextID =>
          if (CSCommonUtils.CONTEXT_ENV_PROD.equalsIgnoreCase(contextID.getEnv)) {
            info(s"reset creator from ${requestPersistTask.getRequestApplicationName} to " + EntranceConfiguration.SCHEDULER_CREATOR.getValue)
            requestPersistTask.setRequestApplicationName(EntranceConfiguration.SCHEDULER_CREATOR.getValue)
          } else {
            info(s"reset creator from ${requestPersistTask.getRequestApplicationName} to " + EntranceConfiguration.FLOW_EXECUTION_CREATOR.getValue)
            requestPersistTask.setRequestApplicationName(EntranceConfiguration.FLOW_EXECUTION_CREATOR.getValue)
          }
        case _ =>
      }
    }
  }


  /**
    * From cs to get variable
    *
    * @param requestPersistTask
    * @return
    */
  def addCSVariable(requestPersistTask: RequestPersistTask): Unit = {
    val variableMap = new mutable.HashMap[String, String]()
    val (contextIDValueStr, nodeNameStr) = getContextInfo(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])

    if (StringUtils.isNotBlank(contextIDValueStr)) {
      info(s"parse variable nodeName:$nodeNameStr")
      val linkisVariableList: util.List[LinkisVariable] = CSVariableService.getInstance().getUpstreamVariables(contextIDValueStr, nodeNameStr);
      if (null != linkisVariableList) {
        linkisVariableList.foreach { linkisVariable =>
          variableMap.put(linkisVariable.getKey, linkisVariable.getValue)
        }
      }
      TaskUtils.addVariableMap(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]], variableMap)
      info(s"parse variable end nodeName:$nodeNameStr")
    }
  }
}
