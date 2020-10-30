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

package com.webank.wedatasphere.linkis.engine

import com.webank.wedatasphere.linkis.scheduler.SchedulerContext

/**
  * Created by enjoyyin on 2018/9/3.
  */
abstract class LockManager(schedulerContext: SchedulerContext) {

  def isLockExist(lock: String): Boolean

  /**
    * Try to lock an Executor in the ExecutorManager. If the lock is successful, it will return the Executor ID as the ID.
    * 尝试去锁住ExecutorManager里的一个Executor，如果锁成功的话，将返回Executor ID作为标识
    * @return
    */
  def tryLock(timeout: Long): Option[String]

  /**
    * Unlock(解锁)
    * @param lock
    */
  def unlock(lock: String): Unit
  //TODO Here you need to have a daemon thread, regularly sweep all the locks given, once the lock timeout, immediately release the lock
  //TODO 这里需要有一个守护线程，定时去扫所有给出去的锁，一旦有锁超时，立马释放锁
}