/*
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

package com.webank.wedatasphere.linkis.jobhistory.receiver

//import com.webank.wedatasphere.linkis.common.utils.Utils
//import com.webank.wedatasphere.linkis.governance.common.entity.task.{RequestInsertTask, RequestOneTask, RequestQueryTask, RequestUpdateTask}
//import com.webank.wedatasphere.linkis.jobhistory.cache.QueryCacheService
//import com.webank.wedatasphere.linkis.jobhistory.service.JobHistoryQueryService
//import com.webank.wedatasphere.linkis.protocol.query.cache._
//import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration

class QueryReceiver extends Receiver {

  private var queryService: JobHistoryQueryService = _
  private var queryCacheService: QueryCacheService = _

  def this(queryService: JobHistoryQueryService, queryCacheService: QueryCacheService) = {
    this()
    this.queryService = queryService
    this.queryCacheService = queryCacheService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case t: RequestInsertTask => queryService.add(message.asInstanceOf[RequestInsertTask])
    case t: RequestUpdateTask => queryService.change(message.asInstanceOf[RequestUpdateTask])
    case t: RequestQueryTask => queryService.query(message.asInstanceOf[RequestQueryTask])
    case t: RequestOneTask => queryService.searchOne(t.getInstance(), t.getExecId, t.getStartTime, t.getEndTime, t.getExecuteApplicationName)
    case requestReadCache: RequestReadCache =>
      val taskResult = queryCacheService.readCache(requestReadCache)
      if (taskResult == null) {
        return new CacheNotFound()
      } else {
        return new CacheTaskResult(taskResult.getResultSet)
      }
    case requestDeleteCache: RequestDeleteCache =>
      Utils.tryCatch {
        queryCacheService.deleteCache(requestDeleteCache)
        return new SuccessDeletedCache()
      } {
        case e: Exception => return new FailedToDeleteCache(e.getMessage)
      }
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
*/
