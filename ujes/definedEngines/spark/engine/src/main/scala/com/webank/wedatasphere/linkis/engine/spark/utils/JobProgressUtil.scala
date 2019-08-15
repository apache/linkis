package com.webank.wedatasphere.linkis.engine.spark.utils

import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import org.apache.spark.{JobExecutionStatus, SparkContext, SparkJobInfo}

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
      getJobProgressInfoByStages(job, sc, jobGroup)
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

    stages.foreach{stageInfo =>
      numTasks += stageInfo.numTasks()
      numActiveTasks += stageInfo.numActiveTasks()
      numFailedTasks += stageInfo.numFailedTasks()
    }
    val numSucceedTasks = numTasks - numActiveTasks - numFailedTasks
    JobProgressInfo(getJobId(job.jobId(), jobGroup), numTasks, numActiveTasks, numFailedTasks, numSucceedTasks)
  }
  private def getJobId( jobId : Int , jobGroup : String ): String = "jobId-" + jobId + "(" + jobGroup + ")"
}
