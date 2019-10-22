package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse, ExecuteRequest}
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.Running

/**
  * 2019-10-10 WilliamZhu(allwefantasy@gmail.com)
  */
class MLSQLEntranceJob extends EntranceExecutionJob with Logging {
  override def jobToExecuteRequest(): ExecuteRequest = {
    new ExecuteRequest with StorePathExecuteRequest with MLSQLJobExecuteRequest {
      override val code: String = MLSQLEntranceJob.this.getTask match {
        case requestPersistTask: RequestPersistTask => requestPersistTask.getExecutionCode
        case _ => null
      }
      override val storePath: String = MLSQLEntranceJob.this.getTask match {
        case requestPersistTask: RequestPersistTask => requestPersistTask.getResultLocation
        case _ => ""
      }
      override val job: Job = MLSQLEntranceJob.this
    }
  }

  //use executor execute MLSQL code (使用executor执行MLSQL脚本代码)
  override def run(): Unit = {
    if (!isScheduled) return
    startTime = System.currentTimeMillis
    Utils.tryAndWarn(transition(Running))
    val executeResponse = Utils.tryCatch(getExecutor.execute(jobToExecuteRequest())) {
      case t: InterruptedException =>
        warn(s"job $toString is interrupted by user!", t)
        ErrorExecuteResponse("job is interrupted by user!", t)
      case t: ErrorExecuteResponse =>
        warn(s"execute job $toString failed!", t)
        ErrorExecuteResponse("execute job failed!", t)
    }
    executeResponse match {
      case r: CompletedExecuteResponse =>
        transitionCompleted(r)
      case _ => logger.error("not completed")
    }
  }
}
