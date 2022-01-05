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
 
package org.apache.linkis.entrance.log

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.{CacheNotReadyException, EntranceErrorCode}
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.scheduler.queue.Job
/**
  * LogManager implementation, using a singleton class
  * LogManager 的实现, 采用单例类进行
  */
class CacheLogManager extends LogManager with Logging {

  override def getLogReader(execId: String): LogReader = {
    var retLogReader: LogReader = null
    this.entranceContext.getOrCreateScheduler().get(execId).foreach {
      case entranceExecutionJob: EntranceExecutionJob =>
        retLogReader = entranceExecutionJob.getLogReader.getOrElse({
          this.synchronized {
            val sharedCache: Cache =
              entranceExecutionJob.getLogWriter.getOrElse(createLogWriter(entranceExecutionJob)) match {
                case cacheLogWriter: CacheLogWriter =>
                  cacheLogWriter.getCache.getOrElse(throw CacheNotReadyException(EntranceErrorCode.CACHE_NOT_READY.getErrCode, EntranceErrorCode.CACHE_NOT_READY.getDesc))
                case _ =>
                  Cache(1)
              }
            val logPath: String = entranceExecutionJob.getJobRequest.getLogPath
            new CacheLogReader(logPath, EntranceConfiguration.DEFAULT_LOG_CHARSET.getValue, sharedCache, entranceExecutionJob.getUser)
          }
        })
        entranceExecutionJob.setLogReader(retLogReader)
      case _ => null
    }
    retLogReader
  }


  override def createLogWriter(job: Job): LogWriter = {
    if (null != job && job.isCompleted) {
      return null
    }
    job match {
      case entranceExecutionJob: EntranceExecutionJob => {
        val cache: Cache = Cache(EntranceConfiguration.DEFAULT_CACHE_MAX.getValue)
        val logPath: String = entranceExecutionJob.getJobRequest.getLogPath
        val cacheLogWriter: CacheLogWriter =
          new CacheLogWriter(logPath, EntranceConfiguration.DEFAULT_LOG_CHARSET.getValue, cache, entranceExecutionJob.getUser)
        entranceExecutionJob.setLogWriter(cacheLogWriter)
        logger.info(s"job ${entranceExecutionJob.getJobRequest.getId} create cacheLogWriter")
        /*val webSocketCacheLogReader: WebSocketCacheLogReader =
          new WebSocketCacheLogReader(logPath, EntranceConfiguration.DEFAULT_LOG_CHARSET.getValue, cache, entranceExecutionJob.getUser)
        entranceExecutionJob.setWebSocketLogReader(webSocketCacheLogReader)
        val webSocketLogWriter: WebSocketLogWriter = new WebSocketLogWriter(entranceExecutionJob, entranceContext.getOrCreateLogListenerBus)
        entranceExecutionJob.setWebSocketLogWriter(webSocketLogWriter)*/
        cacheLogWriter
      }
      case _ => null
    }
  }
}
