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


//package com.webank.wedatasphere.linkis.resourcemanager
//
//import java.util.concurrent.TimeUnit
//
//import com.webank.wedatasphere.linkis.resourcemanager.event.RMEventListenerBus
//import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{NotifyLockRMEvent, NotifyRMEvent}
//import com.webank.wedatasphere.linkis.resourcemanager.notify.{NotifyRMEventPublisher, NotifyRMEventSubscriber, TopicPublisher, TopicSubscriber}
//import com.webank.wedatasphere.linkis.scheduler.Scheduler
//
/**
  * Created by shanhuang on 9/11/18.
  */
//abstract class NotifyEngine {
//
//  val eventScheduler: Scheduler
//  val eventTopic: String
//  val publisher: TopicPublisher[NotifyRMEvent]
//  val subscriber: TopicSubscriber[NotifyRMEvent]
//
//  /**
//    * 尝试去锁住某个事件，一旦锁住成功，这类事件将不再允许notifyToAll
//    * @param event
//    */
//  def tryLock(event: NotifyLockRMEvent): Unit
//
//  /**
//    * 尝试去锁住某个事件，一旦锁住成功，这类事件将不再允许notifyToAll
//    * @param event
//    * @param timeout
//    * @param unit
//    * @return if succeeded
//    */
//  def tryLock(event: NotifyLockRMEvent, timeout: Long, unit: TimeUnit): Boolean
//
//  /**
//    * 解锁某个事件，允许被通知出去
//    * @param event
//    */
//  def unlock(event: NotifyLockRMEvent): Unit
//
//  /**
//    * 通知给所有的RM实例，包括该实例本身
//    * @param event
//    */
//  def notifyToAll(event: NotifyRMEvent): Unit
//
//  /**
//    * 用于初始化操作，第一次启动RM时，将通过该方法，获取到所有的事件，自动放入消费队列，完成初始化操作
//    * @return
//    */
//  def readNotifiedEvents(): Unit
//
//  /**
//    * 删除某个事件
//    * @return
//    */
//  def removeNotifiedEvents(condition: NotifyRMEvent => Boolean): Unit
//
//  /**
//    * 用于接收被通知过来的事件，用户需自己实现获取事件的方法，并调用该方法，用于通知给该RM的所有listener实例
//    * @param event
//    */
//  final protected def receive(event: NotifyRMEvent): Unit = {
//    eventScheduler.submit(event)
//  }
//}
//
//
///**
//  * 该子类自动开启一个线程，不间断的收取通知事件，自动分发给listener实例
//  */
//abstract class AutoNotifyEngine extends NotifyEngine{
//  def start(): Unit
//  def stop(): Unit
//}
//
///**
//  * 该子类需要人工调用收取通知事件，调用receive方法分发给listener实例
//  */
//abstract class ManualNotifyEngine extends NotifyEngine{
//  def read(): NotifyRMEvent
//  def read(n: Int): Array[NotifyRMEvent]
//}
