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
import java.util.{Comparator, Map => JMap}

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{ModuleRegisterEvent, ModuleUnregisterEvent, NotifyRMEvent, NotifyRMEventListener}

import scala.beans.BeanProperty

/**
  * Created by shanhuang on 9/11/18.
  */
class ModuleListener extends NotifyRMEventListener with Logging {
  @BeanProperty
  var mName: String = _
  @BeanProperty
  var eventScope_ID: Int = _

  @BeanProperty
  var eventScope: Int = _

  import ModuleListener._


  override def onNotifyRMEvent(event: NotifyRMEvent): Unit = event match {
    case e: ModuleRegisterEvent => {
      info("ModuleRegisterEvent: " + e)
      moduleInfoCollect.put(e.moduleInfo.moduleInstance.getInstance, e)

    }
    case e: ModuleUnregisterEvent => {
      info("ModuleUnregisterEvent: " + e)
      moduleInfoCollect.remove(e.moduleInstance.getInstance)
    }
    case _ => {
      warn("not found : " + event)
    }
  }

  override def onEventError(event: com.webank.wedatasphere.linkis.common.listener.Event, t: scala.Throwable): scala.Unit = {}

}

class ModuleEventByModuleName extends Comparator[EMModule] {
  override def compare(o1: EMModule, o2: EMModule): Int = {
    o1.moduleName.compareTo(o2.moduleName)
  }
}

/**
  * id
  * 'id': '22_0',
  * 'scheduledTime': 1542953259749,
  * 'startTime': 0,
  * 'endTime': 0,
  * 'state': {
  * 'scala$Enumeration$Val$$i': 1
  * },
  * 'running': False,
  * 'waiting': False,
  * 'scheduled': True,
  * 'completed': False
  */
case class EMModule(@BeanProperty moduleName: String,
                    @BeanProperty instance: String,
                    @BeanProperty createTime: Long,
                    @BeanProperty endTime: Long,
                    @BeanProperty id: String,
                    @BeanProperty state: String,
                    @BeanProperty totalResource: JMap[String, Any],
                    @BeanProperty usedResource: JMap[String, Any],
                    @BeanProperty running: Boolean,
                    @BeanProperty completed: Boolean
                   )


object ModuleListener {

  val moduleInfoCollect: JMap[String, ModuleRegisterEvent] = new ConcurrentHashMap[String, ModuleRegisterEvent]()

  def parseResource(r: String): JMap[String, Any] = {
    val resource: JMap[String, Any] = new util.HashMap

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
}


