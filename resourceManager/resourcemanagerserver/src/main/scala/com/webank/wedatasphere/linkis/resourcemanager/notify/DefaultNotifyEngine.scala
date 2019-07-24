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

//package com.webank.wedatasphere.linkis.resourcemanager.notify
//
//import java.util.concurrent._
//import java.util.concurrent.atomic.AtomicBoolean
//
//import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
//import com.webank.wedatasphere.linkis.resourcemanager.{AutoNotifyEngine, RMContext}
//import com.webank.wedatasphere.linkis.resourcemanager.event.notify._
//import com.webank.wedatasphere.linkis.scheduler.Scheduler
//
//import collection.JavaConversions._
//
//class DefaultNotifyEngine(val eventScheduler: Scheduler) extends AutoNotifyEngine with Logging{
//
//  val eventTopic = "dwc_events"
//  val publisher = NotifyRMEventPublisher(eventTopic)
//  val subscriber = NotifyRMEventSubscriber(eventTopic, eventScheduler)
//  val waitEventScheduler = new ScheduledThreadPoolExecutor(1)
//  val eventToLockMap = new ConcurrentHashMap[NotifyLockRMEvent, ZookeeperDistributedLock]()
//  val waitFutureQueue = new ConcurrentLinkedQueue[(WaitEvent, ScheduledFuture[_])]
//  val globalLock = ZookeeperDistributedLock("dwc_global_lock", "dwc_global_lock")
//
//  subscriber.start()
//  Utils.addShutdownHook(subscriber.stop)
//
//  override def tryLock(event: NotifyLockRMEvent, timeout: Long, unit: TimeUnit): Boolean = {
//    getLock(event).acquire(timeout, unit)
//  }
//
//  override def tryLock(event: NotifyLockRMEvent): Unit = {
//    getLock(event).acquire()
//  }
//
//  def getLock(event: NotifyLockRMEvent) : ZookeeperDistributedLock = {
//    var lock = eventToLockMap.get(event)
//    if (lock == null) {
//      lock = event match {
//        case e: ModuleLock => ZookeeperDistributedLock("dwc_module_lock", event.moduleName)
//        case e: UserLock => ZookeeperDistributedLock("dwc_user_lock", e.user)
//        case _ => globalLock
//      }
//      eventToLockMap.put(event, lock)
//    }
//    lock
//  }
//
//  override def unlock(event: NotifyLockRMEvent): Unit = {
//    val lock = eventToLockMap.get(event)
//    if(lock != null){
//      lock.release()
//      if(lock.localPending == 0) eventToLockMap.remove(event)
//    }
//  }
//
//  override def notifyToAll(event: NotifyRMEvent): Unit = {
//    info("Thread " + Thread.currentThread()  + "prepared to notify")
//    event match {
//      case e: WaitEvent if e.timeOut >= 0 =>
//        val future = waitEventScheduler.schedule(
//          new ClearWaitRunnable(e, publisher),
//          e.timeOut,
//          TimeUnit.MILLISECONDS)
//        waitFutureQueue.offer((e, future))
//        info("Thread " + Thread.currentThread()  + "got wait event, no publish needed")
//        return
//      case e: UserUsedEvent => {
//
//        val waitFutureIterator = waitFutureQueue.iterator()
//        var found = false
//        while(!found && waitFutureIterator.hasNext){
//          val waitFuture = waitFutureIterator.next
//          if(waitFuture._1.clearEvent.ticketId.equals(e.ticketId)){
//            if(waitFuture._2.cancel(false)){
//              waitFutureIterator.remove()
//            } else {
//              warn("Clear event already published, failed to stop.")
//            }
//            found = true
//          }
//        }
//        waitEventScheduler.purge()
//      }
//      case e: ModuleUnregisterEvent =>
//        val waitFutureIterator = waitFutureQueue.iterator()
//        while(waitFutureIterator.hasNext){
//          val waitFuture = waitFutureIterator.next
//          if(waitFuture._1.clearEvent.moduleInstance.equals(e.moduleInstance)){
//            if(waitFuture._2.cancel(false)){
//              waitFutureIterator.remove()
//            } else {
//              warn("Clear event already published, failed to stop.")
//            }
//          }
//        }
//        waitEventScheduler.purge()
//      case _ =>
//    }
//    info("Thread " + Thread.currentThread()  + "started to publish event")
//    publisher.publish(event)
//    info("Thread " + Thread.currentThread()  + "finished publish event")
//  }
//
//  override def readNotifiedEvents(): Unit = {
//    subscriber.readHistory(_ => true)
//  }
//
//  override def start(): Unit = {
//    subscriber.start()
//  }
//
//  override def stop(): Unit = {
//    subscriber.stop()
//  }
//
//  override def removeNotifiedEvents(condition: NotifyRMEvent => Boolean): Unit = {
//    subscriber.readToArray(condition).foreach(publisher.remove)
//  }
//
//  class ClearWaitRunnable(val waitEvent: WaitEvent, val publisher: NotifyRMEventPublisher) extends Runnable{
//    private val started = new AtomicBoolean(false)
//    override def run(): Unit = {
//      started.compareAndSet(false, true)
//      publisher.publish(waitEvent.clearEvent)
//      // remove itself from queue
//      val waitFutureIterator = waitFutureQueue.iterator()
//      while(waitFutureIterator.hasNext){
//        if(waitFutureIterator.next._1 == waitEvent) waitFutureIterator.remove()
//      }
//    }
//    def alreadyStarted() = {
//      started.get()
//    }
//}
//}
//
