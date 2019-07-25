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

import java.util.concurrent.atomic.AtomicBoolean

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{NotifyRMEvent, NotifyRMEventSerializer}
import com.webank.wedatasphere.linkis.scheduler.Scheduler
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by shanhuang on 2019/1/11.
  */
class NotifyRMEventSubscriber(val topic: String, val zk: ZooKeeper, val eventScheduler: Scheduler) extends AutoTopicSubscriber[NotifyRMEvent] with Logging {

  implicit val formats = DefaultFormats + NotifyRMEventSerializer
  private val started = new AtomicBoolean(false)
  private val moduleScopes = new mutable.HashSet[String]
  val historyRoot = "/dwc_events_history"

  try {
    if (zk.exists("/" + topic, false) == null) zk.create("/" + topic, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists(historyRoot, false) == null) zk.create(historyRoot, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists(historyRoot + "/" + topic, false) == null) zk.create(historyRoot + "/" + topic, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  } catch {
    case e: KeeperException => error(s"Failed to create topic[$topic]: ", e)
  }

  def start() = {
    if(started.compareAndSet(false, true)){
      //synchronize event queues, watch for new sub queues
      for(moduleScope <- zk.getChildren("/" + topic, new TopicWatcher)){
        zk.exists("/" + topic + "/" + moduleScope, new DataNodeWatcher(moduleScope))
        moduleScopes.add(moduleScope)
      }
    }else info(s"Subscriber already started.")
  }

  class DataNodeWatcher(val moduleScope: String) extends Watcher{
    override def process(watchedEvent: WatchedEvent): Unit = {
      if(watchedEvent.getType.equals(EventType.NodeDataChanged)){
        val event = deserialize(zk.getData(watchedEvent.getPath, new DataNodeWatcher(moduleScope), null))
        receive(event)
      } else if (watchedEvent.getType.equals(EventType.NodeDeleted)) moduleScopes.remove(moduleScope) else zk.exists(watchedEvent.getPath, new DataNodeWatcher(moduleScope))
    }
  }

  class TopicWatcher extends Watcher{
    override def process(watchedEvent: WatchedEvent): Unit = {
      val subQueues = zk.getChildren("/" + topic, new TopicWatcher)
      if(watchedEvent.getType.equals(EventType.NodeChildrenChanged)) subQueues.foreach{ moduleScope =>
        if(!moduleScopes. contains(moduleScope)){
          moduleScopes.add(moduleScope)
          val event = deserialize(zk.getData("/" + topic + "/" + moduleScope, new DataNodeWatcher(moduleScope), null))
          receive(event)
        }
      }
    }
  }

  def stop() = {
    started.compareAndSet(true, false)
  }

  def readHistory(condition: NotifyRMEvent => Boolean): Unit = {
    info("Started submitting history events.")
    val children = zk.getChildren(historyRoot + "/" + topic, false)
    for(child <- children){
      zk.getChildren(historyRoot + "/" + topic + "/" + child, false).map( element =>{
        deserialize(zk.getData(historyRoot + "/" + topic + "/" + child + "/" + element, false, null))
      })
        .filter(condition)
        .foreach(receive)
    }
    info("Finished submitting history events.")
  }

  def readToArray(condition: NotifyRMEvent => Boolean): Array[NotifyRMEvent] = zk.getChildren("/" + topic, false)
    .map(child => deserialize(zk.getData("/" + topic + "/" + child, false, null)))
    .filter(condition).toArray

  private def deserialize(bytes: Array[Byte]) = {
    val eventJson = new String(bytes)
    info(Thread.currentThread() + "Deserialized event, ready to submit to scheduler: " + eventJson)
    read[NotifyRMEvent](eventJson)
  }

  def serialize(event: NotifyRMEvent) : Array[Byte] = write(event).getBytes

  override def receive(event: NotifyRMEvent): Unit = {
    try {
      //for test
      //println(Thread.currentThread() + "got event:" +  new String(serialize(event)))
      eventScheduler.submit(event)
      info(Thread.currentThread() + "Submitted event to scheduler.")
    } catch {
      case e: Exception => error("dropped event.", e)
    }
  }

}

object NotifyRMEventSubscriber{
  def apply(topic: String, zk: ZooKeeper, eventScheduler: Scheduler): NotifyRMEventSubscriber = new NotifyRMEventSubscriber(topic, zk, eventScheduler)
  def apply(topic: String, eventScheduler: Scheduler): NotifyRMEventSubscriber = new NotifyRMEventSubscriber(topic, ZookeeperUtils.getOrCreateZookeeper(), eventScheduler)
}
