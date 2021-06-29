/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.entrance.timeout

import java.util
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.{EntranceErrorCode, EntranceIllegalParamException}
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label

import scala.collection.JavaConversions._


class JobTimeoutManager extends Logging {
  private[this] final val timeoutJobByName: ConcurrentMap[String, EntranceJob] = new ConcurrentHashMap[String, EntranceJob]
  val timeoutCheck: Boolean = EntranceConfiguration.ENABLE_JOB_TIMEOUT_CHECK.getValue
  val timeoutScanInterval: Int = EntranceConfiguration.TIMEOUT_SCAN_INTERVAL.getValue

  def add(jobKey: String, job: EntranceJob): Unit = {
    info(s"Adding timeout job: ${job.getId()}")
    if (!timeoutJobByName.contains(jobKey)) {
      synchronized {
        if (!timeoutJobByName.contains(jobKey)) {
          timeoutJobByName.put(jobKey, job)
        }
      }
    } else {
      warn(s"Job already exists, invalid addition: ${jobKey}")
    }
  }

  def delete(jobKey: String): Unit = {
    val job = timeoutJobByName.get(jobKey)
    if (null != job) {
      info(s"Deleting Job: ${job.getId()}")
      synchronized {
        job.kill()
        timeoutJobByName.remove(jobKey)
      }
    }
  }

  def jobExist(jobKey: String): Boolean = {
    timeoutJobByName.contains(jobKey)
  }

  def jobCompleteDelete(jobkey: String): Unit = {
    val job = timeoutJobByName.get(jobkey)
    if (job.isCompleted) {
      info(s"Job is complete, delete it now: ${job.getId()}")
      delete(jobkey)
    }
  }

  private def timeoutDetective(): Unit = {
    if (timeoutCheck) {
      def checkAndSwitch(job: EntranceJob): Unit = {
        info(s"Checking whether the job timed out: ${job.getId()}")
        val currentTime = System.currentTimeMillis() / 1000
        val queuingTime = currentTime - job.getScheduledTime / 1000
        val runningTime = currentTime - job.getStartTime / 1000
        if (!job.isCompleted) {
          val jobQueuingTimeoutOpt = job.jobRequest.getLabels.find(i => i.getLabelKey == LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY)
          val jobRunningTimeoutOpt = job.jobRequest.getLabels.find(i => i.getLabelKey == LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY)
          if (jobQueuingTimeoutOpt.isDefined) {
            warn(s"Job queuing timeout, cancel it now: ${job.getId()}")
            if (queuingTime >= jobQueuingTimeoutOpt.get.getStringValue.toInt) job.cancel()
          }
          if (jobRunningTimeoutOpt.isDefined) {
            warn(s"Job running timeout, cancel it now: ${job.getId()}")
            if (runningTime >= jobQueuingTimeoutOpt.get.getStringValue.toInt) job.cancel()
          }
        }
      }

      timeoutJobByName.foreach(item => {
        info(s"Running timeout detection!")
        synchronized {
          jobCompleteDelete(item._1)
          if (jobExist(item._1)) checkAndSwitch(item._2)
        }
      })
    }
  }

  // 线程周期性扫描超时任务
  val woker = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable() {
    override def run(): Unit = {
      Utils.tryCatch {
        timeoutDetective()
      } {
        case t: Throwable =>
          error(s"TimeoutDetective task failed. ${t.getMessage}", t)
      }
    }
  }, 0, timeoutScanInterval, TimeUnit.SECONDS)
}

object JobTimeoutManager {
  // If the timeout label set by the user is invalid, execution is not allowed
  def checkTimeoutLabel(labels: util.Map[String, Label[_]]): Unit = {
    val jobQueuingTimeoutLabel = labels.getOrElse(LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY, null)
    val jobRunningTimeoutLabel = labels.getOrElse(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY, null)
    val posNumPattern = "^[0-9]+$"
    if ((null != jobQueuingTimeoutLabel && !jobQueuingTimeoutLabel.getStringValue.matches(posNumPattern)) ||
      (null != jobRunningTimeoutLabel && !jobRunningTimeoutLabel.getStringValue.matches(posNumPattern))) {
      val msg = s"The task time tag is not set incorrectly, execution is not allowed."
      throw new EntranceIllegalParamException(EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode, EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg)
    }
  }

  def hasTimeoutLabel(entranceJob: EntranceJob): Boolean = {
    val labels = entranceJob.jobRequest.getLabels
    labels.exists(label => label.getLabelKey == LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY ||
      label.getLabelKey == LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY)
  }
}

