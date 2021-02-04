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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import java.lang

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.cs.CSEntranceHelper
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task

/**
  * @author peacewong
  * @date 2020/3/24 18:28
  */
class CSEntranceInterceptor extends EntranceInterceptor with Logging {

  override def apply(task: Task, logAppender: lang.StringBuilder): Task = {
    task match {
      case requestPersistTask: RequestPersistTask =>
        logger.info("Start to execute CSEntranceInterceptor")
        Utils.tryAndWarn(CSEntranceHelper.addCSVariable(requestPersistTask))
        Utils.tryAndWarn(CSEntranceHelper.resetCreator(requestPersistTask))
        Utils.tryAndWarn(CSEntranceHelper.initNodeCSInfo(requestPersistTask))
        logger.info("Finished to execute CSEntranceInterceptor")
      case _ =>
    }
    task
  }
}
