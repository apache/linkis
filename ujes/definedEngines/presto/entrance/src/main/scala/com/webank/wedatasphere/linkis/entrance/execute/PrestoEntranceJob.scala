package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse}
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.Running

/**
 * Created by yogafire on 2020/5/12
 */
class PrestoEntranceJob extends EntranceExecutionJob {

  //FIXME 可以把这个方法抽象进入EntranceExecutionJob
  //use executor execute presto code (使用executor执行jdbc脚本代码)
  override def run(): Unit = {
    if (!isScheduled) return
    startTime = System.currentTimeMillis
    Utils.tryAndWarn(transition(Running))

    val executeResponse = Utils.tryCatch(getExecutor.execute(jobToExecuteRequest())) {
      case t: InterruptedException =>
        warn(s"job $toString is interrupted by user!", t)
        getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateERROR(s"job $toString is interrupted by user! ${t.getMessage}")))
        ErrorExecuteResponse("job is interrupted by user!", t)
      case t: Exception =>
        warn(s"execute job $toString failed!", t)
        getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateERROR(s"execute job $toString failed! ${t.getMessage}")))
        ErrorExecuteResponse("execute job failed!", t)
    }
    executeResponse match {
      case r: CompletedExecuteResponse =>
        setResultSize(0)
        transitionCompleted(r)
      case _ => logger.error("not completed")
    }
  }

}
