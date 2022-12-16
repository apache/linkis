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

import org.apache.linkis.common.utils.Utils
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
) extends FIFOUserConsumer(schedulerContext, executeService, group) {

  override def loop(): Unit = {
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
            JobHistoryHelper.updateBatchInstances(ids)
          }
          Utils.tryQuietly(Thread.sleep(5000))
          return
        }
      case _ =>
    }

    // general logic
    super.loop()

  }

}
