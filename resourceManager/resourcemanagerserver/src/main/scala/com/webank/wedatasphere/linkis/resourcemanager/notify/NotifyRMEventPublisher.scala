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

package com.webank.wedatasphere.linkis.resourcemanager.notify

import java.util.concurrent.ScheduledThreadPoolExecutor

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify._
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper._
import org.json4s._
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions._

/**
  * Created by shanhuang on 9/11/18.
  */
class NotifyRMEventPublisher(val topic: String, val zk: ZooKeeper) extends TopicPublisher[NotifyRMEvent] with Logging {

  implicit val formats = DefaultFormats + NotifyRMEventSerializer
  val historyRoot = "/dwc_events_history"
  val historyScheduler = new ScheduledThreadPoolExecutor(1)

  try {
    if (zk.exists("/" + topic, false) == null) zk.create("/" + topic, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists(historyRoot, false) == null) zk.create(historyRoot, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists(historyRoot + "/" + topic, false) == null) zk.create(historyRoot + "/" + topic, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  } catch {
    case e: KeeperException => error(s"Failed to create topic[$topic]: ", e)
  }


  def publish(event: NotifyRMEvent): Unit = {
    val moduleScope = event.moduleName + "_" + event.eventScope.toString
    val path = "/" + topic + "/" + moduleScope
    val historyPath = historyRoot + path
    if (zk.exists(path, false) == null) {
      zk.create(path, serialize(event), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      zk.create(historyPath, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    } else {
      // should merge with old event
      val oldEvent = deserialize(zk.getData(path, false, null))
      zk.setData(path, serialize(event.merge(oldEvent)), -1)
    }

    recordHistory(event, historyPath)
  }

  def recordHistory(event: NotifyRMEvent, historyPath: String) = {
    info(Thread.currentThread() + "start to record event to history async")
    historyScheduler.submit(new Runnable {
      override def run(): Unit = Utils.tryAndError({
        event match {
          //for special events, don't record
          case moduleUnregisterEvent: ModuleUnregisterEvent =>
            zk.getChildren(historyPath, false).foreach { eventIndex =>
              deserialize(zk.getData(historyPath + "/" + eventIndex, false, null)) match {
                case e: ModuleInstanceEvent if e.moduleInstance.equals(moduleUnregisterEvent.moduleInstance) =>
                  zk.delete(historyPath + "/" + eventIndex, -1)
                case _ =>
              }
            }
          case userReleasedEvent: UserReleasedEvent => deleteByTicketId(userReleasedEvent.ticketId)
          case clearPrdUsedEvent: ClearPrdUsedEvent => deleteByTicketId(clearPrdUsedEvent.ticketId)
          case clearUsedEvent: ClearUsedEvent => deleteByTicketId(clearUsedEvent.ticketId)
          //for normal events, do record
          case ticketIdEvent: TicketIdEvent => zk.create(historyPath + "/" + ticketIdEvent.ticketId, serialize(event), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL)
          case _ => zk.create(historyPath + "/event", serialize(event), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL)
        }
        info(Thread.currentThread() + "finished record event to history async")
      })

      def deleteByTicketId(ticketId: String) = zk.getChildren(historyPath, false).foreach { eventIndex =>
        if (eventIndex.startsWith(ticketId)) zk.delete(historyPath + "/" + eventIndex, -1)
      }
    })
  }

  def serialize(event: NotifyRMEvent): Array[Byte] = {
    val serialized = write(event).getBytes
    info(Thread.currentThread() + "Serialized event, ready to publish: " + serialized)
    serialized
  }

  private def deserialize(bytes: Array[Byte]) = read[NotifyRMEvent](new String(bytes))

  def remove(event: NotifyRMEvent) = ZKUtil.deleteRecursive(zk, "/" + topic + "/" + event.moduleName + "_" + event.eventScope.toString)
}

object NotifyRMEventPublisher {
  def apply(topic: String, zk: ZooKeeper): NotifyRMEventPublisher = new NotifyRMEventPublisher(topic, zk)

  def apply(topic: String): NotifyRMEventPublisher = new NotifyRMEventPublisher(topic, ZookeeperUtils.getOrCreateZookeeper())
}
