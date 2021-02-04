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

package com.webank.wedatasphere.linkis.entrance.io

import java.util

import com.webank.wedatasphere.linkis.entrance.{EntranceContext, EntranceParser}
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEntranceParserBeanAnnotation
import com.webank.wedatasphere.linkis.entranceclient.context.{ClientEntranceParser, ClientTask}
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.scheduler.queue.Job

/**
  * Created by johnnwang on 2019/2/25.
  */
@ClientEntranceParserBeanAnnotation
class IOEntranceParser extends ClientEntranceParser{
  override def parseToJob(task: Task): Job = {
    val job = new IOEntranceJob
    task match {
      case t:ClientTask =>
        job.setTask(t)
        job.setCreator(t.getCreator)
        job.setUser(t.getUser)
        if(t.getParams != null) job.setParams(t.getParams)
    }
    job
  }
}
