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

package com.webank.wedatasphere.linkis.resourcemanager.notify

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.{MemoryResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.resourcemanager.domain._
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import com.webank.wedatasphere.linkis.resourcemanager.event.notify._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write

import scala.collection.mutable.ArrayBuffer

object NotifyRMEventSerializerTest {

  implicit val formats = DefaultFormats + NotifyRMEventSerializer
  var event: NotifyRMEvent = _
  var serialized: String = _

  def main(args: Array[String]): Unit = {

  }

  private def checkResult = {
    serialized = write(event)
    System.out.println(serialized)
    val recovered = read[NotifyRMEvent](serialized)
    System.out.println(write(recovered))
  }
}
