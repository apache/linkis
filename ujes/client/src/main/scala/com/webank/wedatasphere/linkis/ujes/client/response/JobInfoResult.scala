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

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.ujes.client.response

import java.util
import java.util.Date

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESJobException
import com.webank.wedatasphere.linkis.ujes.client.request.{ResultSetListAction, UserAction}
import org.apache.commons.beanutils.BeanUtils

/**
  * created by cooperyang on 2019/5/23.
  */
@DWSHttpMessageResult("/api/rest_j/v\\d+/jobhistory/\\S+/get")
class JobInfoResult extends DWSResult with UserAction with Status {

  private var task: java.util.Map[_, _] = _
  private var requestPersistTask: RequestPersistTask = _
  private var resultSetList: Array[String] = _

  def setTask(task: util.Map[_, _]): Unit = {
    this.task = task
    requestPersistTask = new RequestPersistTask
    val createdTime = task.get("createdTime").asInstanceOf[Long]
    val updatedTime = task.get("updatedTime").asInstanceOf[Long]
    task.remove("createdTime")
    task.remove("updatedTime")
    BeanUtils.populate(requestPersistTask, task)
    requestPersistTask.setCreatedTime(new Date(createdTime))
    requestPersistTask.setUpdatedTime(new Date(updatedTime))
  }

  def getTask = task

  def getRequestPersistTask: RequestPersistTask = requestPersistTask

  def getResultSetList(ujesClient: UJESClient): Array[String] = {
    if(isSucceed && resultSetList == null) synchronized {
      if(resultSetList != null) return resultSetList
      resultSetList = ujesClient.executeUJESJob(ResultSetListAction.builder().set(this).build()) match {
        case resultSetList: ResultSetListResult => resultSetList.getResultSetList
      }
      resultSetList
    } else if(resultSetList != null) resultSetList
    else if(isFailed) throw new UJESJobException(requestPersistTask.getErrCode, requestPersistTask.getErrDesc)
    else throw new UJESJobException(s"job ${requestPersistTask.getTaskID} is still executing with state ${requestPersistTask.getStatus}.")
  }

  override def getJobStatus: String = requestPersistTask.getStatus
}
