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

package com.webank.wedatasphere.linkis.entrance.background

import java.util

import com.google.gson.{JsonObject, JsonParser}
import com.webank.wedatasphere.linkis.server._
import com.webank.wedatasphere.linkis.server.socket.controller.ServerEvent

/**
  * Created by allenlliu on 2018/12/4.
  */
class LoadBackGroundService extends AbstractBackGroundService {

  override val serviceType: String = "load"

  override def operation(serverEvent: ServerEvent): ServerEvent = {
    val params = serverEvent.getData.map { case (k, v) => k -> v.asInstanceOf[Any] }
    //val executionCode = params.get("executionCode").get
    //val jsonCode = params.get("executionCode").get.asInstanceOf[util.Map[String,Object]]
    val executionCode = BDPJettyServerHelper.gson.toJson(params.get("executionCode").get)
    // TODO: Head may be removed（头可能会去掉）
    var newExecutionCode = ""
    val jsonParser = new JsonParser()
    val jsonCode = jsonParser.parse(executionCode.asInstanceOf[String]).asInstanceOf[JsonObject]
    val source = "val source = \"\"\"" + jsonCode.get("source").toString + "\"\"\"\n"
    val destination = jsonCode.get("destination").toString
    var newDestination = "val destination = \"\"\""
    val length = destination.length
    if (length > 6000) {
      newDestination += destination.substring(0, 6000) + "\"\"\"" + "+" + "\"\"\"" + destination.substring(6000, length) + "\"\"\"\n"
    } else {
      newDestination += destination + "\"\"\"\n"
    }
    newExecutionCode += source
    newExecutionCode += newDestination
    newExecutionCode += "com.webank.wedatasphere.linkis.engine.imexport.LoadData.loadDataToTable(spark,source,destination)"
    params.put("executionCode", newExecutionCode)
    print(newExecutionCode)
    val map = new util.HashMap[String, Object]()
    params.foreach(f => map.put(f._1, f._2.asInstanceOf[Object]))
    serverEvent.setData(map)
    serverEvent
  }
}
