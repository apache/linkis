package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorState}
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}
import org.apache.commons.io.IOUtils

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
class EsEntranceExecutorManager(groupFactory: GroupFactory,
                                engineBuilder: EngineBuilder,
                                engineRequester: EngineRequester,
                                engineSelector: EngineSelector,
                                engineManager: EngineManager,
                                entranceExecutorRulers: Array[EntranceExecutorRuler]) extends EntranceExecutorManagerImpl(groupFactory, engineBuilder, engineRequester, engineSelector, engineManager, entranceExecutorRulers) with Logging {

  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceEngine = schedulerEvent match {
    case job: Job =>
      val newEngine = getOrCreateEngineRequester().request(job)
      newEngine.foreach(initialEntranceEngine)
      newEngine.orNull
    case _ => null
  }

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match{
    case event: SchedulerEvent =>
      Some(createExecutor(event))
    case _ => None
  }

  override def shutdown(): Unit = {
    super.shutdown()
    getOrCreateEngineManager.listEngines(engine => ExecutorState.isAvailable(engine.state))
      .foreach(engine => IOUtils.closeQuietly(engine))
  }
}