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

import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by shanhuang on 9/11/18.
  */
object ZookeeperUtils {

  var zookeeper: Option[ZooKeeper] = None

  //  def ensure(path: String, acl: List[ACL], createMode: CreateMode): Unit = {
  //    val nodes = StringUtils.split("/")
  //    (0 to nodes.length - 1).foreach{ i =>
  //      nodes.slice(0, i + 1).mkString("/")
  //    }
  //  }

  def getOrCreateZookeeper(): ZooKeeper = {
    if (zookeeper.isEmpty) zookeeper.synchronized {
      if (zookeeper.isEmpty) zookeeper = Some(createZookeeper) else if (!zookeeper.get.getState.equals(ZooKeeper.States.CONNECTED)) zookeeper = Some(createZookeeper)
    }
    zookeeper.get
  }

  def createZookeeper() = new ZooKeeper(RMConfiguration.ZOOKEEPER_HOST.getValue, RMConfiguration.ZOOKEEPER_TIMEOUT.getValue, new Watcher {
    override def process(watchedEvent: WatchedEvent): Unit = {
      //TODO
    }
  })

}
