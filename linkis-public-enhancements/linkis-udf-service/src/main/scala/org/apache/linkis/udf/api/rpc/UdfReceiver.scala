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

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.rpc.{Receiver, Sender}
import org.apache.linkis.rpc.utils.RPCUtils
import org.apache.linkis.udf.entity.{PythonModuleInfo, PythonModuleInfoVO}
import org.apache.linkis.udf.service.{PythonModuleInfoService, UDFService, UDFTreeService}

import org.apache.commons.beanutils.BeanUtils

import java.{lang, util}

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.duration.Duration
import scala.tools.nsc.interactive.Logger

class UdfReceiver extends Receiver with Logging {

  private var udfTreeService: UDFTreeService = _

  private var udfService: UDFService = _

  // 注⼊PythonModuleInfoService
  private var pythonModuleInfoService: PythonModuleInfoService = _

  def this(udfTreeService: UDFTreeService, udfService: UDFService) = {
    this()
    this.udfTreeService = udfTreeService
    this.udfService = udfService
  }

  def this(
      udfTreeService: UDFTreeService,
      udfService: UDFService,
      pythonModuleInfoService: PythonModuleInfoService
  ) = {
    this(udfTreeService, udfService)
    this.pythonModuleInfoService = pythonModuleInfoService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  def parseModuleInfoVO(info: PythonModuleInfo): PythonModuleInfoVO = {
    // 假设路径格式为 "username/module_name/module_version"
    val vo = new PythonModuleInfoVO()
    vo.setPath(info.getPath)
    vo.setName(info.getName)
    vo.setId(info.getId)
    vo.setCreateUser(info.getCreateUser)
    vo
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = {
    logger.info(s"udfPython message: ${message.getClass.getName}")
    message match {
      case RequestUdfTree(userName, treeType, treeId, treeCategory) =>
        val udfTree = udfTreeService.getTreeById(treeId, userName, treeType, treeCategory)
        new ResponseUdfTree(udfTree)
      case RequestUdfIds(userName, udfIds, treeCategory) =>
        val udfs = udfService.getUDFInfoByIds(udfIds.map(id => new lang.Long(id)), treeCategory)
        new ResponseUdfs(udfs)
      case RequestPythonModuleProtocol(userName, engineType) =>
        val instance: ServiceInstance = RPCUtils.getServiceInstanceFromSender(sender)
        logger.info(
          s"RequestPythonModuleProtocol: userName: $userName, engineType: $engineType, sendInstance: $instance ."
        )
        // 获取Python模块路径列表
        var list = new java.util.ArrayList[String]()
        list.add(engineType)
        list.add("all")
        val infoes: util.List[PythonModuleInfo] =
          pythonModuleInfoService.getPathsByUsernameAndEnginetypes(userName, list)
        // 将路径列表转换为PythonModuleInfo列表
        var voList = new java.util.ArrayList[PythonModuleInfoVO]()
        infoes.asScala.foreach(info => {
          val vo: PythonModuleInfoVO = parseModuleInfoVO(info)
          voList.add(vo)
        })
        new ResponsePythonModuleProtocol(voList)
      case _ =>
    }
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}

}
