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

package com.webank.wedatasphere.linkis.entrance.scheduler.cache

import java.util.concurrent.ExecutorService

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager
import com.webank.wedatasphere.linkis.entrance.utils.JobHistoryHelper
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.exception.SchedulerErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.SuccessExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.Group
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._

class ReadCacheConsumer(schedulerContext: SchedulerContext,
                        executeService: ExecutorService, private var group: Group, persistenceManager: PersistenceManager) extends FIFOUserConsumer(schedulerContext, executeService, group) {

  override protected def loop(): Unit = {
    val event = Option(getConsumeQueue.take())
    event.foreach{
      case job: EntranceJob =>
        job.getTask match {
          case task: RequestPersistTask =>
            Utils.tryCatch {
              val readCacheBefore = TaskUtils.getRuntimeMap(job.getParams).getOrDefault(TaskConstant.READ_CACHE_BEFORE, 300L).asInstanceOf[Long]
              val cacheResult = JobHistoryHelper.getCache(task.getExecutionCode, task.getExecuteApplicationName, task.getUmUser, readCacheBefore)
              if (cacheResult != null && StringUtils.isNotBlank(cacheResult.getResultLocation)) {
                val resultSets = listResults(cacheResult.getResultLocation, job.getUser)
                if (resultSets.size() > 0) {
                  for (resultSet: FsPath <- resultSets) {
                    val alias = FilenameUtils.getBaseName(resultSet.getPath)
                    val output = FsPath.getFsPath(cacheResult.getResultLocation, FilenameUtils.getName(resultSet.getPath)).getSchemaPath
                    persistenceManager.onResultSetCreated(job, new CacheOutputExecuteResponse(alias, output))
                  }
                  persistenceManager.onResultSizeCreated(job, resultSets.size())
                }
                val runtime = TaskUtils.getRuntimeMap(job.getParams)
                runtime.put(TaskConstant.CACHE, false)
                TaskUtils.addRuntimeMap(job.getParams, runtime)
                job.transitionCompleted(new SuccessExecuteResponse(), "Result found in cache")
              } else {
                info("Cache not found, submit to normal consumer.")
                submitToExecute(job)
              }
            }{
              case t: Throwable =>
                warn("Read cache failed, submit to normal consumer: ", t)
                submitToExecute(job)
            }
          case _ =>
        }
      case _ =>
    }
  }

  private def listResults(resultLocation: String, user: String) = {
    val dirPath = FsPath.getFsPath(resultLocation)
    val fileSystem = FSFactory.getFsByProxyUser(dirPath, user).asInstanceOf[FileSystem]
    Utils.tryFinally {
      fileSystem.init(null)
      if (fileSystem.exists(dirPath)) {
        fileSystem.listPathWithError(dirPath).getFsPaths
      } else {
        Lists.newArrayList[FsPath]();
      }
    }(Utils.tryQuietly(fileSystem.close()))
  }

  private def submitToExecute(job: EntranceJob) = {
    val runtime = TaskUtils.getRuntimeMap(job.getParams)
    runtime.put(TaskConstant.READ_FROM_CACHE, false)
    TaskUtils.addRuntimeMap(job.getParams, runtime)
    val groupName = schedulerContext.getOrCreateGroupFactory.getGroupNameByEvent(job)
    val consumer = schedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val index = consumer.getConsumeQueue.offer(job)
    //index.map(getEventId(_, groupName)).foreach(job.setId)
    if (index.isEmpty) throw new SchedulerErrorException(12001, "The submission job failed and the queue is full!(提交作业失败，队列已满！)")
  }

  private def getEventId(index: Int, groupName: String): String = groupName + "_" + index
}
