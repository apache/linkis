package com.webank.wedatasphere.linkis.engine.spark.utils

import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import org.apache.spark.{JobExecutionStatus, SparkContext}
import org.apache.spark.status.api.v1.JobData

/**
  * Created by johnnwang on 2019/5/9.
  */
object JobProgressUtil {
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
      (100 * completedTaskCount.toDouble / taskCount).toFloat
    }
  }

  def getActiveJobProgressInfo(sc:SparkContext,jobGroup : String):Array[JobProgressInfo] = {
    val jobIds = sc.statusTracker.getJobIdsForGroup(jobGroup)
    val activeJobs = jobIds.flatMap { id => sc.statusTracker.getJobInfo(id) }.filter(_.status() == JobExecutionStatus.RUNNING)
    val progressInfos = activeJobs.map { job =>
      val stages = job.stageIds().flatMap(sc.statusTracker.getStageInfo)
      JobProgressInfo(jobGroup, stages.map(_.numTasks()).sum, stages.map(_.numActiveTasks()).sum,stages.map(_.numActiveTasks()).sum, stages.map(_.numFailedTasks()).sum)
    }
    progressInfos
  }

  def getCompletedJobProgressInfo(sc:SparkContext,jobGroup : String):Array[JobProgressInfo] = {
    val jobIds = sc.statusTracker.getJobIdsForGroup(jobGroup)
    val activeJobs = jobIds.flatMap { id => sc.statusTracker.getJobInfo(id) }.filter(_.status() == JobExecutionStatus.SUCCEEDED)
    val progressInfos = activeJobs.map { job =>
      val stages = job.stageIds().flatMap(sc.statusTracker.getStageInfo)
      JobProgressInfo(jobGroup, stages.map(_.numTasks()).sum, stages.map(_.numActiveTasks()).sum,stages.map(_.numActiveTasks()).sum, stages.map(_.numFailedTasks()).sum)
    }
    progressInfos
  }
}
