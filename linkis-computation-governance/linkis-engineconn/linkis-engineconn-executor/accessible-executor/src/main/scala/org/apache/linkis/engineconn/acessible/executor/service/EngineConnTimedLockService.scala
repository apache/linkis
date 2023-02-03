/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  ExecutorLockEvent,
  ExecutorUnLockEvent
}
import org.apache.linkis.engineconn.acessible.executor.lock.EngineConnTimedLock
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.governance.common.exception.engineconn.{
  EngineConnExecutorErrorCode,
  EngineConnExecutorErrorException
}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.{
  RequestEngineLock,
  RequestEngineUnlock,
  ResponseEngineLock,
  ResponseEngineUnlock
}
import org.apache.linkis.manager.common.protocol.engine.EngineLockType
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

class EngineConnTimedLockService extends LockService with Logging {

  private var engineConnLock: EngineConnTimedLock = _
  private var lockString: String = _
  private var lockType: EngineLockType = EngineLockType.Timed

  private def isSupportParallelism: Boolean =
    AccessibleExecutorConfiguration.ENGINECONN_SUPPORT_PARALLELISM

  /**
   * @param lock
   * @return
   */
  override def isLockExist(lock: String = null): Boolean = synchronized {
    if (isSupportParallelism) true
    else if (null != engineConnLock && engineConnLock.isAcquired()) {
      if (StringUtils.isNotBlank(lock)) {
        if (lock.equals(lockString)) true
        else false
      } else {
        false
      }
    } else {
      false
    }

  }

  /**
   * Try to lock an Executor in the ExecutorManager. If the lock is successful, it will return the
   * Executor ID as the ID. 尝试去锁住ExecutorManager里的一个Executor，如果锁成功的话，将返回Executor ID作为标识
   *
   * @return
   */
  @throws[EngineConnExecutorErrorException]
  override def tryLock(requestEngineLock: RequestEngineLock): Option[String] = synchronized {
    if (null != engineConnLock && engineConnLock.isAcquired()) return None
    this.lockType = requestEngineLock.lockType
    lockType match {
      case EngineLockType.Always =>
        timedLock(-1)
      case EngineLockType.Timed =>
        timedLock(requestEngineLock.timeout)
      case o: Any =>
        logger.error("Invalid lockType : " + BDPJettyServerHelper.gson.toJson(o))
        return Some(null)
    }

  }

  private def timedLock(timeout: Long): Option[String] = {

    // Lock is binded to engineconn, so choose default executor
    ExecutorManager.getInstance.getReportExecutor match {
      case accessibleExecutor: AccessibleExecutor =>
        if (logger.isDebugEnabled) {
          logger.debug("try to lock for executor state is " + accessibleExecutor.getStatus)
          logger.debug("try to lock for executor id is " + accessibleExecutor.getId)
        }
        if (NodeStatus.isCompleted(accessibleExecutor.getStatus)) {
          logger.error(s"Cannot to lock completed ${accessibleExecutor.getStatus} stats executor")
          return None
        }
        if (null == engineConnLock) {
          engineConnLock = new EngineConnTimedLock(timeout)
          ExecutorListenerBusContext
            .getExecutorListenerBusContext()
            .getEngineConnAsyncListenerBus
            .addListener(engineConnLock)
          logger.debug("try to lock for executor get new lock " + engineConnLock)
        }
        if (engineConnLock.tryAcquire(accessibleExecutor)) {
          logger.debug("try to lock for tryAcquire is true ")
          this.lockString = engineConnLock.lock.toString
          val executors = ExecutorManager.getInstance.getExecutors.filter(executor =>
            null != executor && !executor.isClosed
          )
          if (null != executors && !executors.isEmpty) {
            executors.foreach(executor =>
              executor match {
                case accessibleExecutor: AccessibleExecutor =>
                  accessibleExecutor.transition(NodeStatus.Idle)
                case _ =>
              }
            )
          } else {
            logger.error("No valid executors while adding lock.")
            accessibleExecutor.transition(NodeStatus.Idle)
          }
          ExecutorListenerBusContext
            .getExecutorListenerBusContext()
            .getEngineConnAsyncListenerBus
            .post(ExecutorLockEvent(accessibleExecutor, lockString))
          Some(lockString)
        } else None
      case _ =>
        val msg = s"Invalid executor or not instance of SensibleEngine."
        logger.error(msg)
        throw new EngineConnExecutorErrorException(
          EngineConnExecutorErrorCode.INVALID_ENGINE_TYPE,
          msg
        )
    }
  }

  /**
   * Unlock(解锁)
   *
   * @param lock
   */
  override def unlock(lock: String): Boolean = synchronized {
    logger.info(
      "try to unlock for lockEntity is " + engineConnLock.toString + ",and lock is " + lock + ",acquired is " + engineConnLock
        .isAcquired()
        .toString
    )
    if (isLockExist(lock)) {
      logger.info(
        s"try to unlock lockEntity : lockString=$lockString,lockedBy=${engineConnLock.lockedBy.getId}"
      )
      engineConnLock.release()
      this.lockString = null
      true
    } else {
      false
    }
  }

  @Receiver
  override def requestUnLock(requestEngineUnlock: RequestEngineUnlock): ResponseEngineUnlock = {
    if (StringUtils.isBlank(requestEngineUnlock.lock)) {
      logger.error("Invalid requestEngineUnlock: ")
      ResponseEngineUnlock(false)
    } else {
      ResponseEngineUnlock(unlock(requestEngineUnlock.lock))
    }
  }

  override def onAddLock(addLockEvent: ExecutorLockEvent): Unit = {}

  override def onReleaseLock(releaseLockEvent: ExecutorUnLockEvent): Unit = {}

  @Receiver
  override def requestLock(requestEngineLock: RequestEngineLock): ResponseEngineLock = {
    super.requestLock(requestEngineLock)
  }

}

class EngineConnConcurrentLockService extends LockService {

  override def isLockExist(lock: String): Boolean = true

  override def tryLock(requestEngineLock: RequestEngineLock): Option[String] = {
    /* ExecutorManager.getInstance().getDefaultExecutor match {
      case accessibleExecutor: AccessibleExecutor =>
        // Concurrent Engine don't change status when get lock, status will change in other rules
//        accessibleExecutor.transition(NodeStatus.Idle)
      case _ =>
    } */
    Some("lock")
  }

  /**
   * Unlock(解锁)
   *
   * @param lock
   */
  override def unlock(lock: String): Boolean = {
    /* ExecutorManager.getInstance().getDefaultExecutor match {
      case accessibleExecutor: AccessibleExecutor =>
        accessibleExecutor.transition(NodeStatus.Unlock)
      case _ =>
    } */
    true
  }

  @Receiver
  override def requestUnLock(requestEngineUnlock: RequestEngineUnlock): ResponseEngineUnlock =
    ResponseEngineUnlock(true)

  override def onAddLock(addLockEvent: ExecutorLockEvent): Unit = {}

  override def onReleaseLock(releaseLockEvent: ExecutorUnLockEvent): Unit = {}

  @Receiver
  override def requestLock(requestEngineLock: RequestEngineLock): ResponseEngineLock = {
    super.requestLock(requestEngineLock)
  }

}
