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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.entrance.event.EntranceEventListener
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener

/**
  * Created by enjoyyin on 2018/9/10.
  */
abstract class EngineManager extends ExecutorListener with EntranceEventListener {

  private val waitLock = new Array[Byte](0)
  protected var entranceExecutorManager: EntranceExecutorManager = _

  def setEntranceExecutorManager(entranceExecutorManager: EntranceExecutorManager): Unit = this.entranceExecutorManager = entranceExecutorManager

  /**
    * The user initializes the operation. When the entance is started for the first time, all the engines are obtained through this method, and the initialization operation is completed.
    * 用户初始化操作，第一次启动entrance时，将通过该方法，拿到所有的engine，完成初始化操作
    */
  def readAliveEngines(): Unit

  def get(id: Long): EntranceEngine

  def get(instance: String): Option[EntranceEngine]

  def listEngines(op: EntranceEngine => Boolean): Array[EntranceEngine]

  def waitForIdle(waitTime: Long): Unit = waitLock synchronized {
    waitLock.wait(waitTime)
  }

  protected def notifyWaiter() = waitLock synchronized waitLock.notify

  def addNotExistsEngines(engine: EntranceEngine*): Unit

  def delete(id: Long): Unit

}