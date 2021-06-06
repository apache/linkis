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

package com.webank.test

import com.webank.wedatasphere.linkis.manager.common.entity.resource.{Resource, ResourceSerializer, ResourceType}
import com.webank.wedatasphere.linkis.manager.common.serializer.NodeResourceSerializer
import com.webank.wedatasphere.linkis.server.Message
import org.codehaus.jackson.map.ObjectMapper
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.collection.mutable


object TestResource {

  implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
  val mapper = new ObjectMapper()

  def main(args: Array[String]): Unit = {
    val message = Message.ok("")
    val creatorToApplicationList = new mutable.HashMap[String, mutable.HashMap[String, Any]]
    val applicationList = new mutable.HashMap[String, Any]
    creatorToApplicationList.put("IDE", applicationList)
    applicationList.put("engineInstances", new mutable.ArrayBuffer[Any]())
    applicationList.put("usedResource", Resource.initResource(ResourceType.LoadInstance))
    applicationList.put("maxResource", Resource.initResource(ResourceType.LoadInstance))
    val engineInstance = new mutable.HashMap[String, Any]
    engineInstance.put("creator", "")
    engineInstance.put("engineType", "")
    engineInstance.put("instance", "")
    engineInstance.put("label", "")
    //engineInstance.put("resource", new CommonNodeResource)
    engineInstance.put("status", "")
    // TODO engineInstance.setStartTime(node)
    applicationList.get("engineInstances").get.asInstanceOf[mutable.ArrayBuffer[Any]].append(engineInstance)
    val a = write(creatorToApplicationList)
    val b = write(engineInstance)
    val c = write(applicationList)
    val d = mapper.readTree(a)
  }

  //  def main(args: Array[String]): Unit = {
  //    val resource1 = new DriverAndYarnResource(new LoadInstanceResource(100,100,0),
  //      new YarnResource(101440 * 1024l * 1024l,50,2,"q05"))
  //    val resource2 = new DriverAndYarnResource(new LoadInstanceResource(50,50,0),
  //      new YarnResource(103424 * 1024l * 1024l,2,0,"q05"))
  //
  //    println((resource1 - resource2).toString)
  //  }
  //  def minus(resource1:Resource,resource2:Resource) = {
  //    resource1 - resource2
  //  }
}
