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

package org.apache.linkis.entrance.timeout

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceIllegalParamException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.entrance.{
  JobQueuingTimeoutLabel,
  JobRunningTimeoutLabel
}

import java.util
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap, TimeUnit}

import scala.collection.JavaConverters._

class JobTimeoutManager extends Logging {

  private[this] final val timeoutJobByName: ConcurrentMap[String, EntranceJob] =
    new ConcurrentHashMap[String, EntranceJob]

  private val timeoutCheck: Boolean = EntranceConfiguration.ENABLE_JOB_TIMEOUT_CHECK.getValue
  private val timeoutScanInterval: Int = EntranceConfiguration.TIMEOUT_SCAN_INTERVAL.getValue

  def add(jobKey: String, job: EntranceJob): Unit = {
    logger.info(s"Adding timeout job: ${job.getId()}")
    if (!timeoutJobByName.containsKey(jobKey)) {
      synchronized {
        if (!timeoutJobByName.containsKey(jobKey)) {
          timeoutJobByName.put(jobKey, job)
        }
      }
    } else {
      logger.warn(s"Job already exists, invalid addition: ${jobKey}")
    }
  }

  def delete(jobKey: String): Unit = {
    val job = timeoutJobByName.get(jobKey)
    if (null != job) {
      logger.info(s"Deleting Job: ${job.getId()}")
      synchronized {
        timeoutJobByName.remove(jobKey)
      }
    }
  }

  def jobExist(jobKey: String): Boolean = {
    timeoutJobByName.containsKey(jobKey)
  }

  def jobCompleteDelete(jobkey: String): Unit = {
    val job = timeoutJobByName.get(jobkey)
    if (job.isCompleted) {
      logger.info(s"Job is complete, delete it now: ${job.getId()}")
      delete(jobkey)
    }
  }

  private def timeoutDetective(): Unit = {
    def checkAndSwitch(job: EntranceJob): Unit = {
      logger.info(s"Checking whether the job id ${job.getJobRequest.getId()} timed out. ")
      val currentTimeSeconds = System.currentTimeMillis() / 1000
      // job.isWaiting == job in queue
      val jobScheduleStartTimeSeconds =
        if (job.isWaiting) job.createTime / 1000 else currentTimeSeconds
      val queuingTimeSeconds = currentTimeSeconds - jobScheduleStartTimeSeconds
      val jobRunningStartTimeSeconds =
        if (job.getStartTime > 0) job.getStartTime / 1000 else currentTimeSeconds
      val runningTimeSeconds = currentTimeSeconds - jobRunningStartTimeSeconds
      if (!job.isCompleted) {
        job.jobRequest.getLabels.asScala foreach {
          case queueTimeOutLabel: JobQueuingTimeoutLabel =>
            if (
                job.isWaiting && queueTimeOutLabel.getQueuingTimeout > 0 && queuingTimeSeconds >= queueTimeOutLabel.getQueuingTimeout
            ) {
              logger.warn(
                s"Job ${job.getJobRequest.getId()} queued time : ${queuingTimeSeconds} seconds, which was over queueTimeOut : ${queueTimeOutLabel.getQueuingTimeout} seconds, cancel it now! "
              )
              job.onFailure(
                s"Job queued ${queuingTimeSeconds} seconds over max queue time : ${queueTimeOutLabel.getQueuingTimeout} seconds.",
                null
              )
            }
          case jobRunningTimeoutLabel: JobRunningTimeoutLabel =>
            if (
                job.isRunning && jobRunningTimeoutLabel.getRunningTimeout > 0 && runningTimeSeconds >= jobRunningTimeoutLabel.getRunningTimeout
            ) {
              logger.warn(
                s"Job ${job.getJobRequest.getId()} run timeout ${runningTimeSeconds} seconds, which was over runTimeOut : ${jobRunningTimeoutLabel.getRunningTimeout} seconds, cancel it now! "
              )
              job.onFailure(
                s"Job run ${runningTimeSeconds} seconds over max run time : ${jobRunningTimeoutLabel.getRunningTimeout} seconds.",
                null
              )
            }
          case _ =>
        }
      }
    }

    timeoutJobByName.asScala.foreach(item => {
      logger.info(s"Running timeout detection!")
      synchronized {
        jobCompleteDelete(item._1)
        if (jobExist(item._1)) checkAndSwitch(item._2)
      }
    })
  }

  // Thread periodic scan timeout task
  if (timeoutCheck) {
    val woker = Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable() {

        override def run(): Unit = {
          Utils.tryCatch {
            timeoutDetective()
          } { case t: Throwable =>
            logger.warn(s"TimeoutDetective task failed. ${t.getMessage}", t)
          }
        }

      },
      0,
      timeoutScanInterval,
      TimeUnit.SECONDS
    )
  }

}

object JobTimeoutManager {

  // If the timeout label set by the user is invalid, execution is not allowed
  def checkTimeoutLabel(labels: util.Map[String, Label[_]]): Unit = {
    val jobQueuingTimeoutLabel =
      labels.getOrDefault(LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY, null)
    val jobRunningTimeoutLabel =
      labels.getOrDefault(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY, null)
    val posNumPattern = "^[0-9]+$"
    if (
        (null != jobQueuingTimeoutLabel && !jobQueuingTimeoutLabel.getStringValue.matches(
          posNumPattern
        )) ||
        (null != jobRunningTimeoutLabel && !jobRunningTimeoutLabel.getStringValue.matches(
          posNumPattern
        ))
    ) {
      val msg = s"The task time tag is not set incorrectly, execution is not allowed."
      throw new EntranceIllegalParamException(
        EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg
      )
    }
  }

  def hasTimeoutLabel(entranceJob: EntranceJob): Boolean = {
    val labels = entranceJob.jobRequest.getLabels
    labels.asScala.exists(label =>
      label.getLabelKey == LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY ||
        label.getLabelKey == LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY
    )
  }

}
