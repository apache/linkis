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
import com.webank.wedatasphere.linkis.engine.annotation.EngineSchedulerContextBeanAnnotation.EngineSchedulerContextAutowiredAnnotation
import com.webank.wedatasphere.linkis.engine.event.EngineEventListenerBus
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOScheduler
import com.webank.wedatasphere.linkis.scheduler.{Scheduler, SchedulerContext}
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by enjoyyin on 2018/10/15.
  */
@Component
class EngineContextImpl extends EngineContext {
  private var scheduler: Scheduler = _
  @Autowired
  private var lockManager: LockManager = _
  @EngineSchedulerContextAutowiredAnnotation
  private var schedulerContext: SchedulerContext = _
  @Autowired
  private var engineParser: EngineParser = _

  @PostConstruct
  def init(): Unit = scheduler = new FIFOScheduler(schedulerContext)

  override def getOrCreateLockManager: LockManager = lockManager

  override def getOrCreateEventListenerBus: EngineEventListenerBus = null //TODO wait for completing

  override def getOrCreateScheduler: Scheduler = scheduler

  override def getOrCreateEngineParser: EngineParser = engineParser
}