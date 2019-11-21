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

package com.webank.wedatasphere.linkis.jobhistory.service.impl

import java.lang.Long
import java.util
import java.util.Date

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.jobhistory.dao.TaskMapper
import com.webank.wedatasphere.linkis.jobhistory.entity.{QueryTask, QueryTaskVO}
import com.webank.wedatasphere.linkis.jobhistory.service.QueryService
import com.webank.wedatasphere.linkis.jobhistory.transitional.TransitionalQueryService
import com.webank.wedatasphere.linkis.protocol.query._
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by johnnwang on 2019/2/25.
  */
@Service
class QueryServiceImpl extends QueryService with Logging {
  @Autowired
  private var taskMapper: TaskMapper = _
  @Autowired
  private var transitionalQueryService:TransitionalQueryService = _

  override def add(requestInsertTask: RequestInsertTask): ResponsePersist = {
    info("Insert data into the database(往数据库中插入数据)：" + requestInsertTask.toString)
    val persist = new ResponsePersist
    Utils.tryCatch {
      val queryTask = requestPersistTaskTask2QueryTask(requestInsertTask)
      taskMapper.insertTask(queryTask)
      val map = new util.HashMap[String, Object]()
      map.put("taskID", queryTask.getTaskID())
      persist.setStatus(0)
      persist.setData(map)
    } {
      case e: Exception =>
        error(e.getMessage)
        persist.setStatus(1);
        persist.setMsg(e.getMessage);
    }
    persist
  }

  override def change(requestUpdateTask: RequestUpdateTask): ResponsePersist = {
    info("Update data to the database(往数据库中更新数据)：" + requestUpdateTask.toString)
    transitionalQueryService.change(requestUpdateTask)
  }

  override def query(requestQueryTask: RequestQueryTask): ResponsePersist = {
    val persist = new ResponsePersist
    Utils.tryCatch {
      val task = taskMapper.selectTask(requestPersistTaskTask2QueryTask(requestQueryTask))
      val map = new util.HashMap[String, Object]()
      map.put("task", queryTaskList2RequestPersistTaskList(task))
      persist.setStatus(0)
      persist.setData(map)
    } {
      case e: Exception =>
        error(e.getMessage)
        persist.setStatus(1);
        persist.setMsg(e.getMessage);
    }
    persist
  }

  private def queryTaskList2RequestPersistTaskList(queryTask: java.util.List[QueryTask]): java.util.List[RequestPersistTask] = {
    import scala.collection.JavaConversions._
    val tasks = new util.ArrayList[RequestPersistTask]
    import com.webank.wedatasphere.linkis.jobhistory.conversions.TaskConversions.queryTask2RequestPersistTask
    queryTask.foreach(f => tasks.add(f))
    tasks
  }

  def requestPersistTaskTask2QueryTask(requestPersistTask: RequestPersistTask): QueryTask = {
    val task: QueryTask = new QueryTask
    BeanUtils.copyProperties(requestPersistTask, task)
    if(requestPersistTask.getSource != null)
      task.setSourceJson(BDPJettyServerHelper.gson.toJson(requestPersistTask.getSource))
    if (requestPersistTask.getParams != null)
      task.setParamsJson(BDPJettyServerHelper.gson.toJson(requestPersistTask.getParams))
    else
      task.setParamsJson(null)
    task
  }

  override def getTaskByID(taskID: Long,userName:String): QueryTaskVO = {
    val task = new QueryTask
    task.setTaskID(taskID)
    task.setUmUser(userName)
    val taskR = taskMapper.selectTask(task)
    if (taskR.size() > 0) {
      import com.webank.wedatasphere.linkis.jobhistory.conversions.TaskConversions.queryTask2QueryTaskVO
      taskR.get(0)
    } else {
      null
    }
  }

  override def search(taskID: Long, username: String, status: String, sDate: Date, eDate: Date, executeApplicationName: String): util.List[QueryTask] = {
    import scala.collection.JavaConversions._
    val split: util.List[String] = if (status != null) status.split(",").toList else null
    taskMapper.search(taskID, username, split, sDate, eDate, executeApplicationName)
  }
}
