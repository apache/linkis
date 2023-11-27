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

package org.apache.linkis.entrance.scheduler

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.entrance.utils.JobHistoryHelper
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.queue.Group
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer

import java.util
import java.util.concurrent.ExecutorService

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class EntranceFIFOUserConsumer(
    schedulerContext: SchedulerContext,
    executeService: ExecutorService,
    private var group: Group
) extends FIFOUserConsumer(schedulerContext, executeService, group)
    with Logging {

  override def loop(): Unit = {
    // When offlineFlag=true, the unsubmitted tasks will be failover, and the running tasks will wait for completion.
    // In this case,super.loop only submits the retry task, but the retry task can failover and speed up the entrance offline
    // (当offlineFlag=true时，未提交任务会被故障转移，运行中任务会等待完成.此时super.loop只会提交重试任务，但是重试任务完全可以故障转移，加快entrance下线)
    schedulerContext match {
      case entranceSchedulerContext: EntranceSchedulerContext =>
        if (
            entranceSchedulerContext.getOfflineFlag && EntranceConfiguration.ENTRANCE_FAILOVER_RETRY_JOB_ENABLED.getValue
        ) {
          val jobs = scanAllRetryJobsAndRemove()
          if (!jobs.isEmpty) {
            val ids = new util.ArrayList[Long]()
            jobs.asScala.foreach {
              case entranceJob: EntranceExecutionJob =>
                entranceJob.getLogWriter.foreach(_.close())
                ids.add(entranceJob.getJobRequest.getId)
              case _ =>
            }
            JobHistoryHelper.updateBatchInstancesEmpty(ids)
          }
          Utils.tryQuietly(Thread.sleep(5000))
          return
        }
      case _ =>
    }

    // general logic
    super.loop()

  }

  override def runScheduleIntercept: Boolean = {
    val consumers = getSchedulerContext.getOrCreateConsumerManager.listConsumers
    var creatorRunningJobNum = 0
    // APP_TEST_hadoop_hive or IDE_hadoop_hive
    val groupNameStr = getGroup.getGroupName
    val groupNames = groupNameStr.split("_")
    val length = groupNames.length
    if (length < 3) return true
    // APP_TEST
    val lastIndex = groupNameStr.lastIndexOf("_")
    val secondLastIndex = groupNameStr.lastIndexOf("_", lastIndex - 1)
    val creatorName = groupNameStr.substring(0, secondLastIndex)
    // hive
    val ecType = groupNames(length - 1)
    for (consumer <- consumers) {
      val groupName = consumer.getGroup.getGroupName
      if (groupName.startsWith(creatorName) && groupName.endsWith(ecType)) {
        creatorRunningJobNum += consumer.getRunningEvents.length
      }
    }
    val creatorECTypeMaxRunningJobs =
      CreatorECTypeDefaultConf.getCreatorECTypeMaxRunningJobs(creatorName, ecType)
    if (logger.isDebugEnabled) {
      logger.debug(
        s"Creator: $creatorName EC:$ecType there are currently:$creatorRunningJobNum jobs running and maximum limit: $creatorECTypeMaxRunningJobs"
      )
    }
    if (creatorRunningJobNum > creatorECTypeMaxRunningJobs) {
      logger.error(
        s"Creator: $creatorName EC:$ecType there are currently:$creatorRunningJobNum  jobs running that exceed the maximum limit: $creatorECTypeMaxRunningJobs"
      )
      false
    } else true
  }

}
