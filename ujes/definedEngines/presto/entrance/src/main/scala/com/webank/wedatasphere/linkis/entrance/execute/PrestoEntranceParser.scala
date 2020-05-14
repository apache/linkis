package com.webank.wedatasphere.linkis.entrance.execute

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.parser.CommonEntranceParser
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.scheduler.queue.Job

/**
 * Created by yogafire on 2020/5/14
 */
class PrestoEntranceParser extends CommonEntranceParser with Logging {

  logger.info("Presto EntranceParser Registered")

  /**
   * Parse a task into an executable job(将一个task解析成一个可执行的job)
   *
   * @param task
   * @return
   */
  override def parseToJob(task: Task): Job = {
    val job = new PrestoEntranceJob
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
