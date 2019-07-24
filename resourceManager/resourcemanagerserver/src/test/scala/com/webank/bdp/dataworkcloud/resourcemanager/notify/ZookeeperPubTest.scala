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

import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope.Instance
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.DefaultNotifyRMEvent

object ZookeeperPubTest {
  def main(args: Array[String]): Unit = {
    val threadPub = new PubThread
    threadPub.start()
  }

}

class PubThread extends Thread {
  val modules = Array("a", "b", "c", "d", "e", "f")
  val scopes = Array(EventScope.Instance, EventScope.Other, EventScope.Service, EventScope.User)
  val publisher = NotifyRMEventPublisher("test_queue")

  override def run(): Unit = {
    (0 to 100).foreach { i =>
      publisher.publish(new DefaultNotifyRMEvent(i.toString, modules(i % modules.length), scopes(i % scopes.length)))
      Thread.sleep(500)
    }
  }
}