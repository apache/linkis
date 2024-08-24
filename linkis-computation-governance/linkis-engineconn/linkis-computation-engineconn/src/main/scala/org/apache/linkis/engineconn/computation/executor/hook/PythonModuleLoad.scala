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

package org.apache.linkis.engineconn.computation.executor.hook

import org.apache.linkis.common.conf.Configuration.IS_VIEW_FS_ENV
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.linkis.rpc.Sender
import org.apache.linkis.udf.UDFClientConfiguration
import org.apache.linkis.udf.api.rpc.{RequestPythonModuleProtocol, ResponsePythonModuleProtocol}
import org.apache.linkis.udf.entity.PythonModuleInfoVO

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * The PythonModuleLoad class is designed to load Python modules into the execution environment
 * dynamically. This class is not an extension of UDFLoad, but shares a similar philosophy of
 * handling dynamic module loading based on user preferences and system configurations.
 */
abstract class PythonModuleLoad extends Logging {

  /** Abstract properties to be defined by the subclass */
  protected val engineType: String
  protected val runType: RunType

  protected def getEngineType(): String = engineType

  protected def constructCode(pythonModuleInfo: PythonModuleInfoVO): String

  private def queryPythonModuleRpc(
      userName: String,
      engineType: String
  ): java.util.List[PythonModuleInfoVO] = {
    val infoList = Sender
      .getSender(UDFClientConfiguration.UDF_SERVICE_NAME.getValue)
      .ask(RequestPythonModuleProtocol(userName, engineType))
      .asInstanceOf[ResponsePythonModuleProtocol]
      .getModulesInfo()
    infoList
  }

  protected def getLoadPythonModuleCode: Array[String] = {
    val engineCreationContext =
      EngineConnManager.getEngineConnManager.getEngineConn.getEngineCreationContext
    val user = engineCreationContext.getUser

    var infoList: util.List[PythonModuleInfoVO] =
      Utils.tryAndWarn(queryPythonModuleRpc(user, getEngineType()))
    if (infoList == null) {
      logger.info("rpc get info is empty.")
      infoList = new util.ArrayList[PythonModuleInfoVO]()
    }

    if (infoList.isEmpty) {
      val pmi = new PythonModuleInfoVO()
      pmi.setPath("viewfs:///apps-data/hadoop/hello_world.py")
      infoList.add(pmi)

      val pmi1 = new PythonModuleInfoVO()
      pmi1.setPath("viewfs:///apps-data/hadoop/redis2.zip")
      infoList.add(pmi1)
    }

    // 替换Viewfs
    if (IS_VIEW_FS_ENV.getValue) {
      infoList.asScala.foreach { info =>
        val path = info.getPath
        info.setPath(path.replace("hdfs://", "viewfs://"))
      }
    }

    logger.info(s"${user} load python modules: ")
    infoList.asScala.foreach(l => logger.info(s"module name:${l.getName}, path:${l.getPath}\n"))

    // 创建加载code
    val codes: mutable.Buffer[String] = infoList.asScala
      .filter { info => StringUtils.isNotEmpty(info.getPath) }
      .map(constructCode)
    // 打印codes
    val str: String = codes.mkString("\n")
    logger.info(s"python codes: $str")
    codes.toArray
  }

  private def executeFunctionCode(codes: Array[String], executor: ComputationExecutor): Unit = {
    if (null == codes || null == executor) {
      return
    }
    codes.foreach { code =>
      logger.info("Submit function registration to engine, code: " + code)
      Utils.tryCatch(executor.executeLine(new EngineExecutionContext(executor), code)) {
        t: Throwable =>
          logger.error("Failed to load python module", t)
          null
      }
    }
  }

  /**
   * Generate and execute the code necessary for loading Python modules.
   *
   * @param executor
   *   An object capable of executing code in the current engine context.
   */
  protected def loadPythonModules(labels: Array[Label[_]]): Unit = {

    val codes = getLoadPythonModuleCode
    logger.info(s"codes length: ${codes.length}")
    if (null != codes && codes.nonEmpty) {
      val executor = ExecutorManager.getInstance.getExecutorByLabels(labels)
      if (executor != null) {
        val className = executor.getClass.getName
        logger.info(s"executor class: ${className}")
      } else {
        logger.error(s"Failed to load python, executor is null")
      }

      executor match {
        case computationExecutor: ComputationExecutor =>
          executeFunctionCode(codes, computationExecutor)
        case _ =>
      }
    }
    logger.info(s"Successful to load python, engineType : ${engineType}")
  }

}

// Note: The actual implementation of methods like `executeFunctionCode` and `construct
