package com.webank.wedatasphere.linkis.entrance.executer

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl
import com.webank.wedatasphere.linkis.scheduler.executer.Executor
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}

import scala.concurrent.duration.Duration

/**
  * 2019-10-10 WilliamZhu(allwefantasy@gmail.com)
  */
class MLSQLEngineExecutorManagerImpl(groupFactory: GroupFactory,
                                     engineBuilder: EngineBuilder,
                                     engineRequester: EngineRequester,
                                     engineSelector: EngineSelector,
                                     engineManager: EngineManager,
                                     entranceExecutorRulers: Array[EntranceExecutorRuler])
  extends EntranceExecutorManagerImpl(groupFactory, engineBuilder, engineRequester,
    engineSelector, engineManager, entranceExecutorRulers) with Logging {
  logger.info("MLSQL EngineManager Registered")

  override protected def createExecutor(event: SchedulerEvent): EntranceEngine = event match {
    case job: MLSQLEntranceJob =>
      val executor = new MLSQLEngineExecutor(5000, job)
      executor.setInstance(event.asInstanceOf[MLSQLEntranceJob].getTask.getInstance)
      executor
  }

  override def setExecutorListener(executorListener: ExecutorListener): Unit = ???

  override def askExecutor(event: SchedulerEvent): Option[Executor] = event match {
    case job: MLSQLEntranceJob =>
      findUsefulExecutor(job).orElse(Some(createExecutor(event)))
    case _ => None
  }


  override def askExecutor(event: SchedulerEvent, wait: Duration): Option[Executor] = event match {
    case job: MLSQLEntranceJob =>
      findUsefulExecutor(job).orElse(Some(createExecutor(event)))
    case _ => None
  }


  private def findUsefulExecutor(job: Job): Option[Executor] = job match {
    case job: MLSQLEntranceJob =>
      Some(createExecutor(job))
  }


  override def getById(id: Long): Option[Executor] = ???

  override def getByGroup(groupName: String): Array[Executor] = ???

  override protected def delete(executor: Executor): Unit = ???

  override def shutdown(): Unit = ???

}
