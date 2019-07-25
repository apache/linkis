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

import java.util.Collections
import java.util.concurrent.{CountDownLatch, Semaphore, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.Logging
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{KeeperException, _}

import scala.collection.JavaConversions._
/**
  * Created by shanhuang on 9/11/18.
  */
class ZookeeperDistributedLock(zk: ZooKeeper, var lockBase: String, lockName: String) extends DistributedLock with Logging{

  if(!lockBase.startsWith("/")) lockBase = "/" + lockBase
  val localLock = new Semaphore(1)
  val waitingBell = new CountDownLatch(1)
  var waitingNodePath = ""

  try {
    if(zk.exists(lockBase, true) == null){
      zk.create(lockBase, "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    }
  } catch {
    case e: KeeperException => error("Fail to create basePath" ,e)
  }

  override def acquire(): Unit = {
    acquire(Long.MaxValue, TimeUnit.MILLISECONDS)
  }

  override def acquire(time: Long, unit: TimeUnit): Boolean = {
    try {
      localLock.tryAcquire(time, unit)
      waitingNodePath = zk.create(s"$lockBase/$lockName", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL)
      val waitingNodeName = waitingNodePath.substring(waitingNodePath.lastIndexOf("/") + 1)
      val acquired = doAcquire(time, unit, waitingNodeName)
      if(!acquired) localLock.release()
      return acquired
    } catch {
      case e: KeeperException =>
        error("Fail to acquire lock", e)
        localLock.release()
        return false
    }
  }

  private def doAcquire(time: Long, unit: TimeUnit, waitingNodeName: String) : Boolean = {
    val pendingNodes = zk.getChildren(lockBase, false)
    Collections.sort(pendingNodes)
    if (waitingNodeName.equals(pendingNodes.get(0))) {
      info(Thread.currentThread.getName + " acquired lock.")
      return true
    }

    val directFrontNode = pendingNodes.reverseIterator.find(_.compareTo(waitingNodeName) < 0).getOrElse("")
    if (directFrontNode.isEmpty) {
      info(Thread.currentThread.getName + " acquired lock.")
      return true
    }

    val stat = zk.exists(lockBase + "/" + directFrontNode, new DirectFrontWatcher(waitingNodeName))
    if (stat == null) {
      return doAcquire(time, unit, waitingNodeName) // Nodes has been updated, try again
    } else if (!waitingBell.await(time, unit)) {
      try {
        info(Thread.currentThread.getName + "waiting for lock time out.")
        zk.delete(waitingNodePath, -1)
      } catch {
        // EPHEMERAL node will be automatically deleted after Zookeeper session timeout. So safely ignore KeeperException here.
        case e: KeeperException => error("", e)
      }
      return false
    }

    info(Thread.currentThread.getName + " acquired lock.")
    return true
  }

  private class DirectFrontWatcher(waitingNodeName: String) extends Watcher {

    override def process(watchedEvent: WatchedEvent): Unit = {
      if (watchedEvent.getType.equals(EventType.NodeDeleted)){
        watchDirectFrontNode(waitingNodeName)
      }
    }

    private def watchDirectFrontNode(waitingNodeName: String) : Unit ={
      val pendingNodes = zk.getChildren(lockBase, false)
      Collections.sort(pendingNodes)
      if (waitingNodeName.equals(pendingNodes.get(0))) {
        waitingBell.countDown()
        return
      }
      val directFrontNode = pendingNodes.reverseIterator.find(_.compareTo(waitingNodeName) < 0).getOrElse("")
      if(directFrontNode.isEmpty){
        waitingBell.countDown()
        return
      }
      val stat = zk.exists(lockBase + "/" + directFrontNode, new DirectFrontWatcher(waitingNodeName))
      if(stat == null){
        watchDirectFrontNode(waitingNodeName) // Nodes has been updated, try again
      }
    }
  }

  override def release(): Unit = {
    try {
      zk.delete(waitingNodePath, -1)
      info(Thread.currentThread.getName + "released lock.")
      localLock.release()
    } catch {
      case e: KeeperException => error("", e)
    }
  }

  def localPending(): Int = {
    localLock.getQueueLength
  }

}

object ZookeeperDistributedLock{
  def apply(basePath: String, lockName: String): ZookeeperDistributedLock = new ZookeeperDistributedLock(ZookeeperUtils.getOrCreateZookeeper(), basePath, lockName)
  def apply(zk: ZooKeeper, basePath: String, lockName: String): ZookeeperDistributedLock = new ZookeeperDistributedLock(zk, basePath, lockName)
}
