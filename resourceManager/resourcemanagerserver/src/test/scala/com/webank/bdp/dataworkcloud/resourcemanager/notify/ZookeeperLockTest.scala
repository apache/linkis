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

object ZookeeperLockTest {

  def main(args: Array[String]): Unit = {

    val thread1 = new TestThread("a")
    val thread2 = new TestThread("b")
    val thread3 = new TestThread("c")
    val thread4 = new TestThread("d")
    val thread5 = new TestThread("e")

    thread1.start()
    thread2.start()
    thread3.start()
    thread4.start()
    thread5.start()

  }

  class TestThread(name: String) extends Thread {
    override def run(): Unit = {
      val lock = ZookeeperDistributedLock("/dwc_test", "test_lock")
      try {
        lock.acquire()
        System.out.println(name + " get lock")
        Thread.sleep(3000)
      } finally {
        lock.release()
        System.out.println(name + " release lock")
      }
    }
  }

}
