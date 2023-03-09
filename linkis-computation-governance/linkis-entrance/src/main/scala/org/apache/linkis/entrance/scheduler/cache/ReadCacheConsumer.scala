/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.scheduler.cache

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.CacheNotReadyException
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.entrance.utils.JobHistoryHelper
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.errorcode.LinkisSchedulerErrorCodeSummary._
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse
import org.apache.linkis.scheduler.queue.Group
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

import java.util.concurrent.ExecutorService

import scala.collection.JavaConverters._

import com.google.common.collect.Lists

class ReadCacheConsumer(
    schedulerContext: SchedulerContext,
    executeService: ExecutorService,
    private var group: Group,
    persistenceManager: PersistenceManager
) extends FIFOUserConsumer(schedulerContext, executeService, group) {

  override protected def loop(): Unit = {
    val event = Option(getConsumeQueue.take())
    event.foreach {
      case job: EntranceJob =>
        job.getJobRequest match {
          case jobRequest: JobRequest =>
            Utils.tryCatch {
              val engineTpyeLabel = jobRequest.getLabels.asScala
                .filter(l => l.getLabelKey.equalsIgnoreCase(LabelKeyConstant.ENGINE_TYPE_KEY))
                .headOption
                .getOrElse(null)
              val labelStrList = jobRequest.getLabels.asScala.map { case l =>
                l.getStringValue
              }.toList
              if (null == engineTpyeLabel) {
                logger.error(
                  "Invalid engineType null, cannot process. jobReq : " + BDPJettyServerHelper.gson
                    .toJson(jobRequest)
                )
                throw CacheNotReadyException(
                  INVALID_ENGINETYPE_NULL.getErrorCode,
                  INVALID_ENGINETYPE_NULL.getErrorDesc
                )
              }
              val readCacheBefore: Long = TaskUtils
                .getRuntimeMap(job.getParams)
                .getOrDefault(TaskConstant.READ_CACHE_BEFORE, 300L: java.lang.Long)
                .asInstanceOf[Long]
              val cacheResult = JobHistoryHelper.getCache(
                jobRequest.getExecutionCode,
                jobRequest.getExecuteUser,
                labelStrList.asJava,
                readCacheBefore
              )
              if (cacheResult != null && StringUtils.isNotBlank(cacheResult.getResultLocation)) {
                val resultSets = listResults(cacheResult.getResultLocation, job.getUser)
                if (resultSets.size() > 0) {
                  for (resultSet: FsPath <- resultSets.asScala) {
                    val alias = FilenameUtils.getBaseName(resultSet.getPath)
                    val output = FsPath
                      .getFsPath(
                        cacheResult.getResultLocation,
                        FilenameUtils.getName(resultSet.getPath)
                      )
                      .getSchemaPath
//                    persistenceManager.onResultSetCreated(job, new CacheOutputExecuteResponse(alias, output))
                    throw CacheNotReadyException(
                      INVALID_RESULTSETS.getErrorCode,
                      INVALID_RESULTSETS.getErrorDesc
                    )
                    // todo check
                  }
//                  persistenceManager.onResultSizeCreated(job, resultSets.size())
                }
                val runtime = TaskUtils.getRuntimeMap(job.getParams)
                runtime.put(TaskConstant.CACHE, java.lang.Boolean.FALSE)
                TaskUtils.addRuntimeMap(job.getParams, runtime)
                job.transitionCompleted(SuccessExecuteResponse(), "Result found in cache")
              } else {
                logger.info("Cache not found, submit to normal consumer.")
                submitToExecute(job)
              }
            } { t =>
              logger.warn("Read cache failed, submit to normal consumer: ", t)
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
        Lists.newArrayList[FsPath]()
      }
    }(Utils.tryQuietly(fileSystem.close()))
  }

  private def submitToExecute(job: EntranceJob): Unit = {
    val runtime = TaskUtils.getRuntimeMap(job.getParams)
    runtime.put(TaskConstant.READ_FROM_CACHE, java.lang.Boolean.FALSE)
    TaskUtils.addRuntimeMap(job.getParams, runtime)
    val groupName = schedulerContext.getOrCreateGroupFactory.getOrCreateGroup(job).getGroupName
    val consumer = schedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val index = consumer.getConsumeQueue.offer(job)
    // index.map(getEventId(_, groupName)).foreach(job.setId)
    if (index.isEmpty) {
      throw new SchedulerErrorException(
        JOB_QUEUE_IS_FULL.getErrorCode,
        JOB_QUEUE_IS_FULL.getErrorDesc
      )
    }
  }

}
