/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.execute.impl

import java.util
import java.util.Map.Entry
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration._
import com.webank.wedatasphere.linkis.entrance.event.{EntranceEvent, MissingEngineNotifyEvent}
import com.webank.wedatasphere.linkis.entrance.execute.{EngineManager, EntranceEngine}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.{Busy, ExecutorState, Idle}
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorState}
import org.apache.commons.io.IOUtils

import scala.collection.JavaConversions._

/**
  * Created by enjoyyin on 2018/9/21.
  */
class EngineManagerImpl extends EngineManager with Logging {

  private val notHealthEngines = new util.HashSet[String]
  private val idToEngines = new util.HashMap[Long, EntranceEngine]
  private val instanceToEngines = new util.HashMap[String, EntranceEngine]
  private val enginesWaitForHeartbeat = new util.ArrayList[EntranceEngine]


  //Every once in a while, scan the engine that has been in the Busy state to ensure that the state is up to date.
  //每隔一段时间，扫描一直处于Busy状态的engine，确保状态是最新的
  Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {

    override def run(): Unit = Utils.tryAndWarn {enginesWaitForHeartbeat.toList.foreach { engine =>
      if(engine.getEngineReturns.nonEmpty) engine.callReconnect()
      else Utils.tryAndWarn{
        engine.refreshState()
        if(notHealthEngines.contains(engine.getModuleInstance.getInstance)) {
          notHealthEngines.remove(engine.getModuleInstance.getInstance)
          warn(s"heartbeat to $engine succeed, now change it from not-health list to normal list.")
        }
      }
    }
    }
  }, 60000, ENGINE_STATUS_HEARTBEAT_TIME.getValue.toLong, TimeUnit.MILLISECONDS)
  //Every once in a while, ask Eureka for the latest engine list and update it to prevent some engines from being broadcasted due to GC or other reasons, or not being consumed in time after being broadcast.
  //每隔一段时间，向Eureka请求最新的引擎列表，进行更新，防止由于GC等原因，导致部分引擎没有广播过来，或广播过来后，没有及时消费
  Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = Utils.tryAndWarn {
      val existsInstances = getInstances
      val needDeleteEngines: Iterator[Entry[Long, EntranceEngine]] = if(existsInstances == null || existsInstances.isEmpty)
        idToEngines.entrySet().iterator
      else idToEngines.entrySet().filter(entry => !existsInstances.contains(entry.getValue.getModuleInstance)
        || ExecutorState.isCompleted(entry.getValue.state)).iterator
      //TODO this is special support for eureka
      needDeleteEngines.filter(entry => System.currentTimeMillis - entry.getValue.getLastActivityTime > 120000).toArray.foreach { entry =>
        warn(s"delete engine ${entry.getValue}, since it is not exists in Eureka or completed with state ${entry.getValue.state}.")
        delete(entry.getKey)
      }
      existsInstances.foreach{instance =>
        if(!instanceToEngines.containsKey(instance.getInstance) && !notHealthEngines.contains(instance.getInstance)) instanceToEngines synchronized {
          if(!instanceToEngines.containsKey(instance.getInstance) && !notHealthEngines.contains(instance.getInstance)) {
            warn(s"add a new engine(${instance.getInstance}), since it is not exists in Entrance list.")
            buildAndAddEngine(instance.getInstance)}
        }
      }
    }
  }, 120000, ENGINE_LIST_FRESH_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  //Every once in a while, scan the engine in the un-health state, if the state is OK, rejoin
  //每隔一段时间，扫描处于un-health状态的engine，如果状态OK了，就重新加入
  Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = Utils.tryAndWarn{
      val existsInstances = getInstances.map(_.getInstance)
      notHealthEngines.toList.foreach{ instance =>
        notHealthEngines.remove(instance)
        if(existsInstances.contains(instance)) buildAndAddEngine(instance)
      }
    }
  }, 120000, UN_HEALTH_ENGINE_SCAN_TIME.getValue.toLong, TimeUnit.MILLISECONDS)

  def buildAndAddEngine(instance: String): Unit = Utils.tryCatch{
    val engine = entranceExecutorManager.getOrCreateEngineBuilder().buildEngine(instance)
    entranceExecutorManager.initialEntranceEngine(engine)
  }{t =>
    notHealthEngines.add(instance)
    warn(s"init engine $instance failed, add it to un-health list.", t)
  }

  protected def getInstances: Array[ServiceInstance] = Sender.getInstances(ENGINE_SPRING_APPLICATION_NAME.getValue)

  /**
    * The user initializes the operation. When the entance is started for the first time, all the engines are obtained through this method, and the initialization operation is completed.
    * 用户初始化操作，第一次启动entrance时，将通过该方法，拿到所有的engine，完成初始化操作
    */
  override def readAliveEngines(): Unit = {
    info("begin to read all alive engines for entrance remark.")
    getInstances.foreach(instance => buildAndAddEngine(instance.getInstance))
    info("all alive engines has been marked.")
  }

  override def get(id: Long): EntranceEngine = idToEngines.get(id)

  override def get(instance: String): Option[EntranceEngine] = Option(instanceToEngines.get(instance))

  override def listEngines(op: EntranceEngine => Boolean): Array[EntranceEngine] =
    idToEngines.entrySet().map(_.getValue).filter(e => !notHealthEngines.contains(e.getModuleInstance.getInstance) && op(e)).toArray

  override def addNotExistsEngines(engine: EntranceEngine*): Unit =
    engine.foreach{e =>
      if(!instanceToEngines.containsKey(e.getModuleInstance.getInstance)) instanceToEngines synchronized {
        if(!instanceToEngines.containsKey(e.getModuleInstance.getInstance)) {
          idToEngines.put(e.getId, e)
          info(toString + "：add a new engine => " + e)
          instanceToEngines.put(e.getModuleInstance.getInstance, e)
        }
      }
    }

  override def delete(id: Long): Unit = if(idToEngines.containsKey(id)) instanceToEngines synchronized {
    if(idToEngines.containsKey(id)) {
      instanceToEngines.remove(idToEngines.get(id).getModuleInstance.getInstance)
      val engine = idToEngines.remove(id)
      IOUtils.closeQuietly(engine)
      if(notHealthEngines.contains(engine.getModuleInstance.getInstance))
        notHealthEngines.remove(engine.getModuleInstance.getInstance)
      info(s"deleted engine $engine.")
    }
  }

  override def onExecutorCreated(executor: Executor): Unit = executor match {
    case engine: EntranceEngine => addNotExistsEngines(engine)
  }

  override def onExecutorCompleted(executor: Executor, message: String): Unit = executor match {
    case engine: EntranceEngine => delete(engine.getId)
  }

  override def onExecutorStateChanged(executor: Executor, fromState: ExecutorState, toState: ExecutorState): Unit = executor match {
    case engine: EntranceEngine =>
      toState match {
        case Idle =>
          if(enginesWaitForHeartbeat.contains(engine))
            enginesWaitForHeartbeat synchronized enginesWaitForHeartbeat.remove(engine)
          this.notifyWaiter()
        case Busy => enginesWaitForHeartbeat.add(engine)
        case state if ExecutorState.isCompleted(state) =>
          if(enginesWaitForHeartbeat.contains(engine))
            enginesWaitForHeartbeat synchronized enginesWaitForHeartbeat.remove(engine)
          delete(executor.getId)
        case _ =>
      }
  }

  override def onEvent(event: EntranceEvent): Unit = event match {
    case MissingEngineNotifyEvent(job, t, executor) =>
      val source = if(job != null) s"Job($job)" else "<unknown>"
      warn(s"received a notify from $source that the $executor may has been missed, now add it to un-health list.", t)
      executor match {
        case entranceEngine: EntranceEngine =>
          notHealthEngines.add(entranceEngine.getModuleInstance.getInstance)
        case _ =>
      }
    case _ =>
  }

  override def onEventError(event: EntranceEvent, t: Throwable): Unit = {
    error(s"deal event $event failed!", t)
  }
}
