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
package com.webank.wedatasphere.linkis.entrance.parser

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.execute.JDBCEntranceJob
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.scheduler.queue.Job

class JDBCEntranceParser extends CommonEntranceParser with Logging {
  logger.info("JDBC EntranceParser Registered")

  /**
    * Parse a task into an executable job(将一个task解析成一个可执行的job)
    *
    * @param task
    * @return
    */
  override def parseToJob(task: Task): Job = {
    val job = new JDBCEntranceJob
    task match {
      case requestPersistTask: RequestPersistTask =>
        job.setTask(task)
        job.setUser(requestPersistTask.getUmUser)
        job.setCreator(requestPersistTask.getRequestApplicationName)
        job.setParams(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])
        job.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
        job.setListenerEventBus(null)
        job.setProgress(0f)
    }
    job
  }
}
