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

package com.webank.wedatasphere.linkis.enginemanager.impl

import java.util.concurrent.{ConcurrentHashMap, ScheduledFuture, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngine
import com.webank.wedatasphere.linkis.enginemanager.{Engine, EngineFactory, EngineManagerReceiver}
import com.webank.wedatasphere.linkis.protocol.engine.{EngineState, RequestKillEngine}
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.resourcemanager.{Resource, UserResultResource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.server.toScalaMap

/**
  * Created by johnnwang on 2018/10/10.
  * @param rmClient
  */
class EngineFactoryImpl(rmClient: ResourceManagerClient) extends EngineFactory with Logging {

  private val portToEngines = new ConcurrentHashMap[Int, Engine]
  private val futures = new Array[ScheduledFuture[_]](2)

  /**
    * Start the following thread:
  * 1. Time release the current node idle engine
    * 启动以下线程：
    * 1. 定时释放当前节点空闲引擎
    */
  override def init(): Unit = {
    //Remove the suicide engine(去掉已经自杀的engine)
    futures(0) = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        //The first step is to remove the engine that has been completed.(第一步，去掉已经completed的引擎)
        portToEngines.filter{case (_, engine) => EngineState.isCompleted(engine.getState)}.foreach{
          case (_, engine) =>
            warn(s"$engine is completed with state ${engine.getState}, now remove it.")
            delete(engine)
        }
        //The second step is to remove the suicide engine.(第二步，去掉已经自杀的engine)
        //TODO special support for eureka
        val aliveEngines = portToEngines.filter{ case (_, engine) => EngineState.isAvailable(engine.getState) &&
          engine.getInitedTime > 0 && System.currentTimeMillis - engine.getInitedTime > ENGINE_CAN_SCAN_AFTER_INIT.getValue.toLong}
        val existsEngineInstances = Sender.getInstances(ENGINE_SPRING_APPLICATION_NAME.getValue).map(_.getInstance)
        aliveEngines.filter{ case (port, engine) =>
            val instance = EngineManagerReceiver.getInstanceByPort(port)
            if(!existsEngineInstances.contains(instance)) {
              warn(s"$engine has already been dead, now remove it, and unregister it for RM.")
              delete(engine)
              false
            } else true
        }.foreach { case (_, engine: ProcessEngine) =>
          //The third step is to remove the engine that is not responding.(第三步、去掉没有响应的engine)
          Utils.tryCatch(engine.tryHeartbeat()) { t =>
            warn(s"heartbeat to $engine failed, now mark it as Failed, and unregister it for RM.", t)
            delete(engine)
          }
        }
      }
    }, ENGINE_SCAN_INTERVAL.getValue.toLong, ENGINE_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)

    //Kill the engines that have lost control(将已经失去掌控的engines，杀掉)
    futures(1) = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val existsEngineInstances = Sender.getInstances(ENGINE_SPRING_APPLICATION_NAME.getValue)
        existsEngineInstances.filter(f => EngineManagerReceiver.isEngineBelongTo(f.getInstance))
          .foreach{ moduleInstance =>
            val port = moduleInstance.getInstance.substring(moduleInstance.getInstance.lastIndexOf(":") + 1)
            if(!portToEngines.containsKey(port.toInt)) {
              warn(s"${moduleInstance.getInstance} is not exists in this EM, now try to kill it with RPC command.")
              Utils.tryAndWarn(Sender.getSender(moduleInstance).send(RequestKillEngine(moduleInstance.getInstance, Sender.getThisServiceInstance.getApplicationName, Sender.getThisServiceInstance.getInstance)))
            }
          }
      }
    }, UNKNOWN_ENGINE_SCAN_INTERVAL.getValue.toLong, UNKNOWN_ENGINE_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  override def get(port: Int): Option[Engine] = {
    Option(portToEngines.get(port))
  }

  override def getUsedResources: Option[Resource] = if(portToEngines.isEmpty) None else {
    val engines = list().filter(_.getResource != null)
    if(engines.length == 1) Option(engines.head.getResource)
    else Some(engines.tail.map(_.getResource).fold(engines.head.getResource)(_ + _))
  }

  override def list(): Array[Engine] = portToEngines.values().toArray(new Array[Engine](portToEngines.size()))

  override def deleteAll(creator: String): Unit = portToEngines synchronized portToEngines.filter{case (_, engine) => engine.getCreator == creator}.foreach(f => delete(f._2))

  override def delete(engine: Engine): Unit =
    if(portToEngines.containsKey(engine.getPort)) {
      val removed = portToEngines.remove(engine.getPort)
      if(removed != null) {
        info(s"ready to delete $engine.")
        Utils.tryAndWarn(removed.shutdown())
        Utils.tryAndWarn(rmClient.resourceReleased(UserResultResource(removed.getTicketId, removed.getUser)))
        info(s"deleted $removed.")
      }
    }

  override def shutdown(deleteEngines: Boolean): Unit = {
    futures.foreach(_.cancel(true))
    if (deleteEngines) {
      portToEngines.toArray.foreach { case (_, engine) =>
        delete(engine)
      }
      portToEngines.clear()
    }
  }

  override def addEngine(engine: Engine): Unit = if(portToEngines.containsKey(engine.getPort))
    warn(engine + " is already exists, please be notify and won't add it in this time.")
  else portToEngines.putIfAbsent(engine.getPort, engine)
}
