/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.cache.GlobalConfigurationKeyValueCache
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.LogPathCreateException
import org.apache.linkis.entrance.parser.ParserUtils
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.protocol.task.Task

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
