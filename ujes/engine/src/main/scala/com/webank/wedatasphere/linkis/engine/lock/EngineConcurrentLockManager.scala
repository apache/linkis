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

import com.webank.wedatasphere.linkis.engine.LockManager
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext

/**
  * Created by enjoyyin on 2018/11/6.
  */
class EngineConcurrentLockManager(schedulerContext: SchedulerContext) extends LockManager(schedulerContext) {

  override def isLockExist(lock: String): Boolean = true

  /**
    * Try to lock an Executor in the ExecutorManager. If the lock is successful, it will return the Executor ID as the ID.
    * 尝试去锁住ExecutorManager里的一个Executor，如果锁成功的话，将返回Executor ID作为标识
    *
    * @return
    */
  override def tryLock(timeout: Long): Option[String] = Some("111")

  /**
    * Unlock(解锁)
    *
    * @param lock
    */
  override def unlock(lock: String): Unit = {}
}
