/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.udf.api.rpc

import org.apache.linkis.rpc.{Receiver, Sender}
import org.apache.linkis.udf.service.{PythonModuleInfoService, UDFService, UDFTreeService}

import java.lang
import scala.concurrent.duration.Duration

class UdfReceiver extends Receiver {

  private var udfTreeService: UDFTreeService = _

  private var udfService: UDFService = _

  def this(udfTreeService: UDFTreeService, udfService: UDFService) = {
    this()
    this.udfTreeService = udfTreeService
    this.udfService = udfService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  // 注⼊PythonModuleInfoService
//  val pythonModuleInfoService: PythonModuleInfoService = context.system.asInstanceOf[ExtendedActorSystem].lifecycle.systemManager.asInstanceOf[SystemManager].pythonModuleInfoService
//
//  def parseModuleInfoFromPath(path: String): PythonModuleInfoVO = {
//    // 假设路径格式为 "username/module_name/module_version"
//    val parts = path.split("/")
//    var vo = PythonModuleInfoVO()
//    vo.setPath(path)
//    vo
//  }

  override def receiveAndReply(message: Any, sender: Sender): Any = {
    message match {
      case RequestUdfTree(userName, treeType, treeId, treeCategory) =>
        val udfTree = udfTreeService.getTreeById(treeId, userName, treeType, treeCategory)
        new ResponseUdfTree(udfTree)
      case RequestUdfIds(userName, udfIds, treeCategory) =>
        val udfs = udfService.getUDFInfoByIds(udfIds.map(id => new lang.Long(id)), treeCategory)
        new ResponseUdfs(udfs)
//      case RequestPythonModuleProtocol(userName, engineTypes) =>
//        // 获取Python模块路径列表
//        val paths = pythonModuleInfoService.getPathsByUsernameAndEnginetypes(userName, engineTypes)
//        // 将路径列表转换为PythonModuleInfo列表
//        val pythonModuleInfoList = paths.map(parseModuleInfoFromPath)
//        new ResponsePythonModuleProtocol(pythonModuleInfoList)
      case _ =>
    }
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}

}