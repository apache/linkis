package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.scheduler.queue.Job

trait MLSQLJobExecuteRequest {
  val job:Job
}
