/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.jobhistory.conversions

import java.io.File
import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.protocol.query.{RequestPersistTask, RequestQueryTask}
import com.webank.wedatasphere.linkis.protocol.utils.ZuulEntranceUtils
import com.webank.wedatasphere.linkis.jobhistory.entity.{QueryTask, QueryTaskVO}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.springframework.beans.BeanUtils
import org.springframework.util.StringUtils

/**
  * Created by johnnwang on 2019/2/25.
  */
object TaskConversions extends Logging{

  implicit def requestQueryTask2QueryTask(requestQueryTask: RequestQueryTask): QueryTask = {
    val task: QueryTask = new QueryTask
    BeanUtils.copyProperties(requestQueryTask, task)
    if (requestQueryTask.getParams != null)
      task.setParamsJson(BDPJettyServerHelper.gson.toJson(requestQueryTask.getParams))
    else
      task.setParamsJson(null)
    task
  }

  implicit def queryTask2RequestPersistTask(queryTask: QueryTask): RequestPersistTask = {
    val task = new RequestPersistTask
    BeanUtils.copyProperties(queryTask, task)
    task.setSource(BDPJettyServerHelper.gson.fromJson(queryTask.getSourceJson, classOf[java.util.HashMap[String, String]]))
    task.setParams(BDPJettyServerHelper.gson.fromJson(queryTask.getParamsJson, classOf[java.util.HashMap[String, Object]]))
    task
  }

  implicit def requestPersistTaskTask2QueryTask(requestPersistTask: RequestPersistTask): QueryTask = {
    val task: QueryTask = new QueryTask
    BeanUtils.copyProperties(requestPersistTask, task)
    if (requestPersistTask.getParams != null)
      task.setParamsJson(BDPJettyServerHelper.gson.toJson(requestPersistTask.getParams))
    else
      task.setParamsJson(null)
    task
  }

  implicit def queryTask2QueryTaskVO(queryTask: QueryTask): QueryTaskVO = {
    val taskVO = new QueryTaskVO
    BeanUtils.copyProperties(queryTask, taskVO)
    if (queryTask.getExecId() != null && queryTask.getExecuteApplicationName() != null && queryTask.getInstance() != null) {
      taskVO.setStrongerExecId(ZuulEntranceUtils.generateExecID(queryTask.getExecId(),
        queryTask.getExecuteApplicationName(), queryTask.getInstance(), queryTask.getRequestApplicationName))
    }
    val status = queryTask.getStatus()
    val createdTime = queryTask.getCreatedTime()
    val updatedTime = queryTask.getUpdatedTime()
    if ("Succeed".equalsIgnoreCase(status) || "Failed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
      if (createdTime != null && updatedTime != null) {
        taskVO.setCostTime(queryTask.getUpdatedTime().getTime() - queryTask.getCreatedTime().getTime());
      } else {
        taskVO.setCostTime(null)
      }
    } else {
      if (createdTime != null) {
        taskVO.setCostTime(System.currentTimeMillis() - queryTask.getCreatedTime().getTime());
      } else {
        taskVO.setCostTime(null)
      }
    }
    taskVO
  }
}
