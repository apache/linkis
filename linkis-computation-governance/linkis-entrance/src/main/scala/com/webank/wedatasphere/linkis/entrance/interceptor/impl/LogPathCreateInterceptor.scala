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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.cache.GlobalConfigurationKeyValueCache
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.LogPathCreateException
import com.webank.wedatasphere.linkis.entrance.parser.ParserUtils
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task

/**
  * Description:Log path generation interceptor, used to set the path log of the task(日志路径生成拦截器, 用于设置task的路径日志)
  */
class LogPathCreateInterceptor extends EntranceInterceptor with Logging {

  @throws[ErrorException]
  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    jobRequest match {
      case jobReq: JobRequest => Utils.tryThrow {
        ParserUtils.generateLogPath(jobReq, Utils.tryAndWarn(GlobalConfigurationKeyValueCache.getCacheMap(jobReq)))
        jobReq
      } {
        case e: ErrorException =>
          val exception: LogPathCreateException = LogPathCreateException(20075, "Failed to get logPath(获取logPath失败)，reason: " + e.getMessage)
          exception.initCause(e)
          exception
        case t: Throwable =>
          val exception: LogPathCreateException = LogPathCreateException(20075, "Failed to get logPath(获取logPath失败), reason: " + t.getCause)
          exception.initCause(t)
          exception
      }
      case _ => jobRequest
    }
  }
}
