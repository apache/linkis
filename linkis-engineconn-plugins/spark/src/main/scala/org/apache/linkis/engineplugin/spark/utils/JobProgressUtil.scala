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
 
package org.apache.linkis.engineplugin.spark.utils

import java.text.NumberFormat

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.spark.{JobExecutionStatus, SparkContext, SparkJobInfo}

/**
  *
  */
object JobProgressUtil extends Logging{
  def progress(sc: SparkContext, jobGroup : String):Float = {
    val jobIds = sc.statusTracker.getJobIdsForGroup(jobGroup)
    val jobs = jobIds.flatMap { id => sc.statusTracker.getJobInfo(id) }
    val stages = jobs.flatMap { job =>
      job.stageIds().flatMap(sc.statusTracker.getStageInfo)
    }

    val taskCount = stages.map(_.numTasks).sum
    val completedTaskCount = stages.map(_.numCompletedTasks).sum
    if (taskCount == 0) {
      0f
    } else {
      (completedTaskCount.toDouble / taskCount).toFloat
    }
  }

  def getActiveJobProgressInfo(sc:SparkContext,jobGroup : String):Array[JobProgressInfo] = {
    val jobIds = sc.statusTracker.getJobIdsForGroup(jobGroup)
    val activeJobs = jobIds.flatMap { id => sc.statusTracker.getJobInfo(id) }.filter(_.status() == JobExecutionStatus.RUNNING)
    val progressInfos = activeJobs.map { job =>
      val jobProgressInfo = getJobProgressInfoByStages(job, sc, jobGroup)
      val timestamp = DateFormatUtils.format(System.currentTimeMillis, "yyyy-MM-dd HH:mm:ss")
      val progress = jobProgressInfo.succeedTasks * 1d /  jobProgressInfo.totalTasks
      info(s"${jobProgressInfo.id} numTasks = ${jobProgressInfo.totalTasks}, numCompletedTasks = ${jobProgressInfo.succeedTasks}," +
        s" numActiveTasks = ${jobProgressInfo.runningTasks} , completed:${percentageFormat(progress)}")
      jobProgressInfo
    }
    progressInfos
  }

  def getCompletedJobProgressInfo(sc:SparkContext,jobGroup : String):Array[JobProgressInfo] = {
    val jobIds = sc.statusTracker.getJobIdsForGroup(jobGroup)
    val completedJobs = jobIds.flatMap { id => sc.statusTracker.getJobInfo(id) }.filter(_.status() == JobExecutionStatus.SUCCEEDED)
    val progressInfos = completedJobs.map { job =>
      getJobProgressInfoByStages(job, sc, jobGroup)
    }
    progressInfos
  }

  private  def getJobProgressInfoByStages(job:SparkJobInfo, sc:SparkContext, jobGroup : String) : JobProgressInfo = {
    val stages = job.stageIds().flatMap(sc.statusTracker.getStageInfo)

    var numTasks = 0
    var numActiveTasks = 0
    var numFailedTasks = 0
    var numSucceedTasks = 0
    stages.foreach{stageInfo =>
      if (stageInfo.submissionTime() > 0){
        numTasks += stageInfo.numTasks()
        numActiveTasks += stageInfo.numActiveTasks()
        numFailedTasks += stageInfo.numFailedTasks()
        numSucceedTasks += stageInfo.numCompletedTasks()
      }
    }
    JobProgressInfo(getJobId(job.jobId(), jobGroup), numTasks, numActiveTasks, numFailedTasks, numSucceedTasks)
  }

 private def getJobId( jobId : Int , jobGroup : String ): String = "jobId-" + jobId + "(" + jobGroup + ")"

  private var _percentFormat: NumberFormat = _

  def percentageFormat(decimal: Double): String = {
    if(_percentFormat == null) {
      _percentFormat = NumberFormat.getPercentInstance
      _percentFormat.setMinimumFractionDigits(2)
    }
    _percentFormat.format(decimal)
  }
}
