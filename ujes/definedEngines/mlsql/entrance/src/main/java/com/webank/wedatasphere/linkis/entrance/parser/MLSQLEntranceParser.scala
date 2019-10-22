package com.webank.wedatasphere.linkis.entrance.parser

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.execute.MLSQLEntranceJob
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.scheduler.queue.Job

/**
  * 2019-10-10 WilliamZhu(allwefantasy@gmail.com)
  */
class MLSQLEntranceParser extends CommonEntranceParser with Logging {
  logger.info("MLSQL EntranceParser Registered")

  override def parseToJob(task: Task): Job = {
    val job = new MLSQLEntranceJob
    task match {
      case requestPersistTask: RequestPersistTask =>
        job.setTask(task)
        job.setUser(requestPersistTask.getUmUser)
        job.setCreator(requestPersistTask.getRequestApplicationName)
        job.setParams(requestPersistTask.getParams.asInstanceOf[util.Map[String, Any]])
        job.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
        job.setListenerEventBus(null)
        job.setProgress(0f)
    }
    job
  }
}
