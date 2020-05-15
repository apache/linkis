package com.webank.wedatasphere.linkis.entrance.execute

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.event.EntranceEvent
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.service.annotation.{EnableResourceManager, RegisterResource}
import com.webank.wedatasphere.linkis.scheduler.executer.Executor
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
@EnableResourceManager
class EsEngineManager(resources: ModuleInfo) extends EngineManager with Logging {

  private val idToEngines = new util.HashMap[Long, EntranceEngine]

  // TODO 修正 EntranceEngine 使用的 resources



  /**
   * The user initializes the operation. When the entance is started for the first time, all the engines are obtained through this method, and the initialization operation is completed.
   * 用户初始化操作，第一次启动entrance时，将通过该方法，拿到所有的engine，完成初始化操作
   */
  override def readAliveEngines(): Unit = { }

  override def get(id: Long): EntranceEngine = ???

  override def get(instance: String): Option[EntranceEngine] = ???

  override def listEngines(op: EntranceEngine => Boolean): Array[EntranceEngine] = ???

  override def addNotExistsEngines(engine: EntranceEngine*): Unit = {}

  override def delete(id: Long): Unit = ???

  override def onEvent(event: EntranceEvent): Unit = { }

  override def onEventError(event: EntranceEvent, t: Throwable): Unit = {
    error(s"deal event $event failed!", t)
  }

  override def onExecutorCreated(executor: Executor): Unit = {}

  override def onExecutorCompleted(executor: Executor, message: String): Unit = {}

  override def onExecutorStateChanged(executor: Executor, fromState: ExecutorState, toState: ExecutorState): Unit = {}

  /**
   * Registered resources(注册资源)
   */
  @RegisterResource
  def registerResources(): ModuleInfo = resources

}
