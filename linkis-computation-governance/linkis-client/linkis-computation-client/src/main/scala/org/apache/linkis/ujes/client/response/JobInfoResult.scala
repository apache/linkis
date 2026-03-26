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

package org.apache.linkis.ujes.client.response

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult
import org.apache.linkis.ujes.client.UJESClient
import org.apache.linkis.ujes.client.exception.UJESJobException
import org.apache.linkis.ujes.client.request.{ResultSetListAction, UserAction}

import org.apache.commons.beanutils.BeanUtils

import java.io.File
import java.nio.file.Files
import java.util
import java.util.Date

import scala.util.matching.Regex

@DWSHttpMessageResult("/api/rest_j/v\\d+/jobhistory/\\S+/get")
class JobInfoResult extends DWSResult with UserAction with Status {

  private var task: java.util.Map[_, _] = _
  private var requestPersistTask: RequestPersistTask = _
  private var resultSetList: Array[String] = _

  private var strongerExecId: String = _

  def setTask(task: util.Map[_, _]): Unit = {
    this.task = task
    requestPersistTask = new RequestPersistTask
    val createdTime = task.get("createdTime").asInstanceOf[Long]
    val updatedTime = task.get("updatedTime").asInstanceOf[Long]
    task.remove("createdTime")
    task.remove("updatedTime")
    task.remove("engineStartTime")
    task.remove("labels")
    Utils.tryCatch {
      BeanUtils.populate(requestPersistTask, task.asInstanceOf[util.Map[String, _]])
    } { case e: Exception =>
      logger.error("copy failed", e)
    }
    requestPersistTask.setStatus(task.get("status").asInstanceOf[String])
    requestPersistTask.setCreatedTime(new Date(createdTime))
    requestPersistTask.setUpdatedTime(new Date(updatedTime))
    requestPersistTask.setEngineStartTime(new Date(updatedTime))
    if (task.containsKey("strongerExecId")) {
      this.strongerExecId = task.get("strongerExecId").asInstanceOf[String]
    }

  }

  def getTask: java.util.Map[_, _] = task

  def getRequestPersistTask: RequestPersistTask = requestPersistTask

  def getStrongerExecId: String = {
    this.strongerExecId
  }

  def getResultSetList(ujesClient: UJESClient): Array[String] = {
    if (isSucceed && resultSetList == null) synchronized {
      if (resultSetList != null) return resultSetList
      resultSetList =
        ujesClient.executeUJESJob(ResultSetListAction.builder().set(this).build()) match {
          case resultSetList: ResultSetListResult => resultSetList.getResultSetList
        }
      val numberRegex: Regex = """(\d+)""".r
      // There are compatibility issues under Windows, which can be resolved through this method.
      // fileName.split(java.util.regex.Pattern.quote(File.separator)).last
      return resultSetList.sortBy { fileName =>
        numberRegex.findFirstIn(fileName.split(File.separator).last).getOrElse("0").toInt
      }
    }
    else if (resultSetList != null) resultSetList
    else if (isFailed) {
      throw new UJESJobException(requestPersistTask.getErrCode, requestPersistTask.getErrDesc)
    } else {
      throw new UJESJobException(
        s"job ${requestPersistTask.getTaskID} is still executing with state ${requestPersistTask.getStatus}."
      )
    }
  }

  override def getJobStatus: String = requestPersistTask.getStatus

  def canRetry: Boolean = {
    val canRetryFlag = "canRetry"
    if (null != task && task.containsKey(canRetryFlag)) {
      task.get(canRetryFlag).asInstanceOf[Boolean]
    } else {
      false
    }
  }

}
