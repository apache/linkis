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

package com.webank.wedatasphere.linkis.engine.lock

import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.scheduler.queue.{ConsumerManager, GroupFactory}

object EngineTimedLockManagerTest {

  val lockManager = new EngineTimedLockManager(new SchedulerContext(){
    override def getOrCreateGroupFactory: GroupFactory = null

    override def getOrCreateConsumerManager: ConsumerManager = null

    override def getOrCreateExecutorManager: ExecutorManager = null

    override def getOrCreateSchedulerListenerBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = null
  })

  val executor = new TestExecutor(111);

  def main(args: Array[String]): Unit = {

    val t1 = new TestThread(executor, "a",1200)
    val t2 = new TestThread(executor, "b",800)
    val t3 = new TestThread(executor, "c",600)
    val t4 = new TestThread(executor, "d",2000)
    val t5 = new TestThread(executor, "e",200)

    t1.start()
    t2.start()
    t3.start()
    t4.start()
    t5.start()
  }
}

class TestThread(val executor: Executor, val name: String, val sleep: Long) extends Thread{
  override def run(): Unit = {
    while(lockManager.tryLock(1000).isEmpty){
      System.out.println(name + "failed get lock, waiting")
      Thread.sleep(sleep)
    }
    System.out.println(name + "get lock, sleep " + sleep)
    Thread.sleep(sleep)
    lockManager.unlock(executor.getId.toString)
    System.out.println(name + "released lock")
  }
}

class TestExecutor(val id: Long) extends Executor{
  override def getId: Long = id

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = null

  override def state: ExecutorState = null

  override def getExecutorInfo: ExecutorInfo = null

  override def close(): Unit = {}
}
