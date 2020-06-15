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

import com.webank.wedatasphere.linkis.common.utils.Logging
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, KeeperException, ZKUtil, ZooKeeper}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by shanhuang on 9/11/18.
  */
class ZookeeperDistributedQueue(zk: ZooKeeper, var queueName: String) extends DistributedQueue[Array[Byte]] with Logging {

  if (!queueName.startsWith("/")) queueName = "/" + queueName

  try
      if (zk.exists(queueName, false) == null) zk.create(queueName, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  catch {
    case e: KeeperException => error(s"Failed to create queue[$queueName]: ", e)
  }

  override def offer(value: Array[Byte]): Unit = {
    zk.create(queueName + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL)
  }

  override def poll(): Array[Byte] = {
    val path = head()
    if (path == null) return null
    val value = zk.getData(path, false, null)
    zk.delete(path, -1)
    value
  }

  override def peek(): Array[Byte] = {
    val path = head()
    if (path == null) return null
    zk.getData(path, false, null)
  }

  override def destroy(): Unit = {
    try
        if (zk.exists(queueName, false) == null) info(s"Queue[$queueName] already destroyed.") else ZKUtil.deleteRecursive(zk, queueName)
    catch {
      case e: KeeperException => error(s"Failed to destroy queue[$queueName]: ", e)
    }
  }

  private def head(): String = {
    val elements = zk.getChildren(queueName, false)
    if (elements.size == 0) return null

    Collections.sort(elements)
    queueName + "/" + elements.get(0)
  }

  override def copyToArray(): Array[Array[Byte]] = {
    val elements = zk.getChildren(queueName, false)
    if (elements.size == 0) return new Array[Array[Byte]](0)
    elements.map({ e => zk.getData(queueName + "/" + e, false, null) }).toArray
  }

  def indexOf(bytes: Array[Byte]): String = {
    val elements = zk.getChildren(queueName, false)
    elements.find(e => bytes.equals(zk.getData(queueName + "/" + e, false, null))).getOrElse("")
  }

  def copyToMap(): mutable.Map[String, Array[Byte]] = {
    val resultMap = mutable.Map.empty[String, Array[Byte]]
    val elements = zk.getChildren(queueName, false)
    if (elements.size == 0) return resultMap
    elements.map(e => resultMap.put(e, zk.getData(queueName + "/" + e, false, null)))
    resultMap
  }

  def remove(index: String) = if (index.length != 0) zk.delete(queueName + "/" + index, -1)
}

object ZookeeperDistributedQueue {
  def apply(queueName: String): ZookeeperDistributedQueue = new ZookeeperDistributedQueue(ZookeeperUtils.getOrCreateZookeeper(), queueName)

  def apply(zk: ZooKeeper, queueName: String): ZookeeperDistributedQueue = new ZookeeperDistributedQueue(zk, queueName)
}
