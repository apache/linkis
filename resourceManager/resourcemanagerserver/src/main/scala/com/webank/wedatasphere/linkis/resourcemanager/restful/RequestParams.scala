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

package com.webank.wedatasphere.linkis.resourcemanager.restful

import java.util
import java.util.Map

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy.ResourceRequestPolicy
import com.webank.wedatasphere.linkis.resourcemanager._
import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * Created by shanhuang on 2019/1/11.
  */
trait RequestParams  extends Logging {
  private def isDigits(digits: String) = if (digits != null && digits.matches("\\d+")) true
  else false

  private def isIP(ip: String) = {
    val ipRegex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
    ip.matches(ipRegex)
  }

  //  protected def getModuleInstance(reqParam: util.Map[String, String]): ModuleInstance = {
  //    var port = 0
  //    var ip: String = null
  //    if (isIP(reqParam.get("ip"))) {
  //      ip = reqParam.get("ip")
  //    }
  //    if (isDigits(reqParam.get("port"))) port = reqParam.get("port").toInt
  //    val name = reqParam.get("name")
  //    val mi = ModuleInstance(ip, port, name)
  //    mi
  //  }

  protected def getResourceRequestPolicy(reqParam: util.Map[String, String]): ResourceRequestPolicy = {
    val rrp = reqParam.get("resourceRequestPolicy")
    var value: ResourceRequestPolicy = null;
    rrp match {
      case "Memory" =>
        value = ResourceRequestPolicy.Memory

      case "CPU" =>
        value = ResourceRequestPolicy.CPU

      case "Load" =>
        value = ResourceRequestPolicy.Load

      case "Instance" =>
        value = ResourceRequestPolicy.Instance

      case "LoadInstance" =>
        value = ResourceRequestPolicy.LoadInstance

      case "Special" =>
        value = ResourceRequestPolicy.Special

    }
    value
  }

  protected def getResource(rep_resource: String): (Resource,String) = {
    //    val totalResource = reqParam.get("totalResource")
    val json = parse(rep_resource)
    //确保隐式转换成功
    implicit val formats = DefaultFormats
    val resource = json match {
      case JObject(List(("memory", memory))) => (new MemoryResource(memory.extract[Long]),"memory")
      case JObject(List(("cores", cores))) => (new CPUResource(cores.extract[Int]),"cpu")
      case JObject(List(("instance", instances))) => (new InstanceResource(instances.extract[Int]),"instance")
      case JObject(List(("memory", memory), ("cores", cores))) =>( new LoadResource(memory.extract[Long], cores.extract[Int]),"Load")
      case JObject(List(("memory", memory), ("cores", cores), ("instance", instances))) =>
        (new LoadInstanceResource(memory.extract[Long], cores.extract[Int], instances.extract[Int]),"loadInstance")
      case JObject(List(("queueName", queueName),("queueMemory", queueMemory), ("queueCores", queueCores), ("queueInstances", queueInstances))) =>
        (new YarnResource(queueMemory.extract[Long], queueCores.extract[Int], queueInstances.extract[Int], queueName.extract[String]),"yarn")
      case JObject(List(("memory", memory), ("cores", cores), ("instance", instances),
      ("queueName", queueName),("queueMemory", queueMemory), ("queueCores", queueCores), ("queueInstances", queueInstances))) =>
        (new DriverAndYarnResource(new LoadInstanceResource(memory.extract[Long], cores.extract[Int], instances.extract[Int]),
          new YarnResource(queueMemory.extract[Long], queueCores.extract[Int], queueInstances.extract[Int], queueName.extract[String])),"driverAndYarn")

      case JObject(List(("resources", resources))) =>
        ( new SpecialResource(resources.extract[Map[String, AnyVal]]),"special")
    }
    resource
  }
  //protected TimeUnit parseTime(String time)
}
