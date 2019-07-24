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

package com.webank.wedatasphere.linkis.entranceclient.context

import java.util

import com.webank.wedatasphere.linkis.entrance.{EntranceContext, EntranceParser}
import com.webank.wedatasphere.linkis.entranceclient.execute
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.server._

/**
  * Created by johnnwang on 2018/10/30.
  */
class ClientEntranceParser extends EntranceParser {
  override def getEntranceContext: EntranceContext = null

  override def setEntranceContext(entranceContext: EntranceContext): Unit = null

  override def parseToTask(params: util.Map[String, Any]): Task = {
    val task = new ClientTask
    task.setInstance(Sender.getThisInstance)
    task.setCode(params.get(TaskConstant.EXECUTIONCODE).asInstanceOf[String])
    task.setUser(params.get(TaskConstant.UMUSER).asInstanceOf[String])
    task.setCreator(params.get(TaskConstant.REQUESTAPPLICATIONNAME).asInstanceOf[String])
    if(params.containsKey(TaskConstant.PARAMS)) params.get(TaskConstant.PARAMS) match {
      case map: java.util.Map[String, Any] =>
        task.setParams(map.filter(_._2 != null))
      case _ =>
    }
    task
  }

  override def parseToTask(job: Job): Task = job match {
    case j: execute.ClientJob => j.getTask
  }

  override def parseToJob(task: Task): Job = {
    val job = new execute.ClientJob
    task match {
      case t: ClientTask =>
        job.setTask(t)
        job.setCreator(t.getCreator)
        job.setUser(t.getUser)
        if(t.getParams != null) job.setParams(t.getParams)
    }
    job
  }
}
