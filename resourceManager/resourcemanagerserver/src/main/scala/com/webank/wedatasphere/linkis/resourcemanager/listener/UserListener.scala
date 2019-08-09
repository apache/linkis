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

package com.webank.wedatasphere.linkis.resourcemanager.listener

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.{Comparator, List => JList, Map => JMap}

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.event.metric.{MetricRMEvent, MetricRMEventListener, UserSessionEndEvent, UserSessionStartEvent}

import scala.beans.BeanProperty

/**
  * Created by shanhuang on 9/11/18.
  */
class UserListener extends MetricRMEventListener with Logging {

  import UserListener._

  override def onMetricRMEvent(e: MetricRMEvent): Unit = e match {
    case e: UserSessionStartEvent => {
      info("[Add a startup event(添加启动事件)]user:" + e.user + ",UserSessionStartEvent: " + e)

      userSessionCollect.put(e.userUsedResource.moduleInstance.getInstance, e)

    }
    case e: UserSessionEndEvent => {
      info("[Cleanup event(清理结束事件)]user:" + e.user + ",UserSessionEndEvent: " + e)
      userSessionCollect.remove(e.userReleasedResource.moduleInstance)
    }
    case _ => {
      warn("not found : " + e)
    }
  }

  def onEventError(event: Event, t: scala.Throwable): scala.Unit = {}
}

class userEventByUserName extends Comparator[UserSessionStartEvent] {
  override def compare(o1: UserSessionStartEvent, o2: UserSessionStartEvent): Int = {
    o1.user.compareTo(o2.user)
  }
}

case class UserSessionInfo(@BeanProperty userName: String,
                           @BeanProperty id: String,
                           @BeanProperty state: String,
                           @BeanProperty running: Boolean,
                           @BeanProperty completed: Boolean,
                           @BeanProperty moduleName: String,
                           @BeanProperty instance: String,
                           @BeanProperty resource: JMap[String, Any],
                           @BeanProperty ticketId: String
                          )

object UserListener {
  val userSessionCollect: JMap[String, UserSessionStartEvent] = new ConcurrentHashMap[String, UserSessionStartEvent]()

  def parseResource(r: String): JMap[String, Any] = {
    val resource: JMap[String, Any] = new util.HashMap
    if (r == null) return resource;
    val r0 = r.split(",|:")
    var i = 0
    while (i < r0.length) {
      if (i == 0) {
        resource.put("type", r0(i))
        i = i + 1
      } else {
        resource.put(r0(i), r0(i + 1))
        i = i + 2
      }
    }
    resource
  }

  def getUserEvent(userName: String): JList[UserSessionInfo] = {
    val eventIterator = userSessionCollect.values().iterator()
    val userList: JList[UserSessionInfo] = new util.ArrayList[UserSessionInfo]();
    while (eventIterator.hasNext) {
      val e = eventIterator.next()
      if (e.user.equals(userName)) {
        val usi = UserSessionInfo(e.user, e.getId, e.getState.toString, e.isRunning, e.isCompleted, e.userUsedResource.moduleInstance.getApplicationName
          , e.userUsedResource.moduleInstance.getInstance, parseResource(e.userUsedResource.resource.toString), e.userUsedResource.ticketId)
        userList.add(usi)
      }
    }
    userList
  }
}