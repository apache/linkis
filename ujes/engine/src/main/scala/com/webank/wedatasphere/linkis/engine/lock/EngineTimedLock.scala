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

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, Semaphore, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.Logging

class EngineTimedLock(val timeout: Long) extends TimedLock with Logging{

  val lock = new Semaphore(1)
  val releaseScheduler = new ScheduledThreadPoolExecutor(1)
  var releaseTask: ScheduledFuture[_] = null
  var lastLockTime: Long = 0
  var lockedBy: Thread = null

  override def acquire(): Unit = {
    lock.acquire()
    lastLockTime = System.currentTimeMillis()
    lockedBy = Thread.currentThread()
    scheduleTimeout
  }

  override def tryAcquire(): Boolean = {
    val succeed = lock.tryAcquire()
    debug("try to lock for succeed is  "+ succeed.toString)
    if(succeed) {
      lastLockTime = System.currentTimeMillis()
      lockedBy = Thread.currentThread()
      debug("try to lock for add time out task !")
      scheduleTimeout
    }
    succeed
  }

  override def release(): Unit = {
    debug("try to release for lock,"+ lockedBy+",current thread "+Thread.currentThread().getName)
    if(lockedBy != null ){
      //&& lockedBy == Thread.currentThread()   Inconsistent thread(线程不一致)
      debug("try to release for lockedBy and thread ")
      if(releaseTask != null){
        releaseTask.cancel(true)
        releaseTask = null
        releaseScheduler.purge()
      }
      lock.release()
      debug("try to release for lock release success")
      lockedBy = null
    }
  }


  override def forceRelease(): Unit = {
    if(isAcquired()){
      if(releaseTask != null){
        releaseTask.cancel(true)
        releaseTask = null
        releaseScheduler.purge()
      }
      lock.release()
      if(lockedBy != null){
        lockedBy.interrupt()
      }
      lockedBy = null
    }
  }

  private def scheduleTimeout: Unit = {
    releaseTask = releaseScheduler.schedule(new Runnable {
        override def run(): Unit = {
          lock.release()
          lockedBy = null
        }
      }, timeout, TimeUnit.MILLISECONDS)
  }

  override def isAcquired(): Boolean = {
    lock.availablePermits() < 1
  }

  override def isExpired(): Boolean = {
    if(lastLockTime == 0) return false
    System.currentTimeMillis() - lastLockTime > timeout
  }


  override def numOfPending(): Int = {
    lock.getQueueLength
  }

  override def renew(): Boolean = {
    if(lockedBy != null && lockedBy == Thread.currentThread()){
      if(isAcquired && releaseTask != null){
        if(releaseTask.cancel(false)){
          releaseScheduler.purge()
          scheduleTimeout
          lastLockTime = System.currentTimeMillis()
          return true
        }
      }
    }
    return false
  }

}
