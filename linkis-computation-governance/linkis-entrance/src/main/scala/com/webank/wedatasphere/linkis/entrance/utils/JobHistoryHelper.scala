/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.utils

import java.util

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.JobHistoryFailedException
import com.webank.wedatasphere.linkis.governance.common.entity.task.{RequestPersistTask, RequestQueryTask, RequestUpdateTask, ResponsePersist}
import com.webank.wedatasphere.linkis.protocol.query.cache.{CacheTaskResult, RequestReadCache}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState


object JobHistoryHelper extends Logging{

  private val sender = Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue)

  private val SUCCESS_FLAG = 0
  private val TASK_MAP_KEY = "task"

  def getCache(executionCode: String, engineType: String, user: String, readCacheBefore: Long): CacheTaskResult ={
    val requestReadCache = new RequestReadCache(executionCode, engineType, user, readCacheBefore)
    sender.ask(requestReadCache) match {
      case  c: CacheTaskResult => c
      case _ => null
    }
  }

  def getStatusByTaskID(taskID:Long):String = {
    val task = getTaskByTaskID(taskID)
    if (task == null) SchedulerEventState.Cancelled.toString else task.getStatus
  }

  /**
   * 对于一个在内存中找不到这个任务的话，可以直接干掉
   * @param taskID
   */
  def forceKill(taskID:Long):Unit = {
    val requestUpdateTask = new RequestUpdateTask
    requestUpdateTask.setTaskID(taskID)
    requestUpdateTask.setStatus("Cancelled")
    sender.ask(requestUpdateTask)
  }

  private def getTaskByTaskID(taskID:Long):RequestPersistTask = {
    val requestQueryTask = new RequestQueryTask()
    requestQueryTask.setTaskID(taskID)
    requestQueryTask.setSource(null)
    val task = Utils.tryCatch{
      val taskResponse = sender.ask(requestQueryTask)
      taskResponse match {
        case responsePersist:ResponsePersist => val status = responsePersist.getStatus
          if (status != SUCCESS_FLAG){
            logger.error(s"query from jobHistory status failed, status is $status")
            throw JobHistoryFailedException("query from jobHistory status failed")
          }else{
            val data = responsePersist.getData
            data.get(TASK_MAP_KEY) match {
              case tasks: util.List[RequestPersistTask] => tasks.get(0)
              case _ => throw JobHistoryFailedException(s"query from jobhistory not a correct List type taskId is $taskID")
            }
          }
        case _ => logger.error("get query response incorrectly")
          throw JobHistoryFailedException("get query response incorrectly")
      }
    }{
      case errorException:ErrorException => throw errorException
      case e:Exception => val e1 = JobHistoryFailedException(s"query taskId $taskID error")
        e1.initCause(e)
        throw e
    }
    task
  }


}
