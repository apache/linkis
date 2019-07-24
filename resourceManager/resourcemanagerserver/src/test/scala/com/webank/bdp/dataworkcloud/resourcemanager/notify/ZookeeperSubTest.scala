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

import com.webank.wedatasphere.linkis.resourcemanager.schedule.{EventSchedulerContextImpl, EventSchedulerImpl}

object ZookeeperSubTest {
  def main(args: Array[String]): Unit = {
    val threadSub = new SubThread
    val threadSub1 = new SubThread

    threadSub.start()
    threadSub1.start()
  }
}

class SubThread extends Thread {
  val subscriber = NotifyRMEventSubscriber("test_queue", new EventSchedulerImpl(new EventSchedulerContextImpl(1)))

  override def run(): Unit = {
    subscriber.start()
    Thread.sleep(60000)
    subscriber.stop()
  }
}
