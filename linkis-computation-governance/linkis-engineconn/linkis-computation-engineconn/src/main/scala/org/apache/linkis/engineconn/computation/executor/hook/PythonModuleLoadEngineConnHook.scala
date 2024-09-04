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
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.hadoop.common.utils.HDFSUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}
import org.apache.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.linkis.rpc.Sender
import org.apache.linkis.udf.UDFClientConfiguration
import org.apache.linkis.udf.api.rpc.{RequestPythonModuleProtocol, ResponsePythonModuleProtocol}
import org.apache.linkis.udf.entity.PythonModuleInfoVO

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

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

    // 替换Viewfs
    if (IS_VIEW_FS_ENV.getValue) {
      infoList.asScala.foreach { info =>
        val path = info.getPath
        logger.info(s"python path: ${path}")
        if (path.startsWith("hdfs") || path.startsWith("viewfs")) {
          info.setPath(path.replace("hdfs://", "viewfs://"))
        } else {
          info.setPath("viewfs://" + path)
        }
      }
    } else {

      infoList.asScala.foreach { info =>
        val path = info.getPath
        logger.info(s"hdfs python path: ${path}")
        if (!path.startsWith("hdfs")) {
          info.setPath("hdfs://" + path)
        }
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

abstract class PythonModuleLoadEngineConnHook
    extends PythonModuleLoad
    with EngineConnHook
    with Logging {

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    Utils.tryAndWarnMsg {
      val codeLanguageLabel = new CodeLanguageLabel
      codeLanguageLabel.setCodeType(runType.toString)
      logger.info(s"engineType: ${engineType}")
      val labels = Array[Label[_]](codeLanguageLabel)
      loadPythonModules(labels)
    }(s"Failed to load Python Modules: ${engineType}")

  }

  override def afterEngineServerStartFailed(
      engineCreationContext: EngineCreationContext,
      throwable: Throwable
  ): Unit = {
    logger.error(s"Failed to start Engine Server: ${throwable.getMessage}", throwable)
  }

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    logger.info("Preparing to load Python Module...")
  }

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    logger.info(s"Before executing command on load Python Module.")
  }

}

// 加载PySpark的Python模块
class PythonSparkEngineHook extends PythonModuleLoadEngineConnHook {

  // 设置engineType属性为"spark"，表示此挂钩适用于Spark数据处理引擎
  override val engineType: String = "spark"

  // 设置runType属性为RunType.PYSPARK，表示此挂钩将执行PySpark类型的代码
  override protected val runType: RunType = RunType.PYSPARK

  // 重写constructCode方法，用于根据Python模块信息构造加载模块的代码
  override protected def constructCode(pythonModuleInfo: PythonModuleInfoVO): String = {
    // 使用pythonModuleInfo的path属性，构造SparkContext.addPyFile的命令字符串
    // 这个命令在PySpark环境中将模块文件添加到所有worker上，以便在代码中可以使用
    val path: String = pythonModuleInfo.getPath
    val loadCode = s"sc.addPyFile('${path}')"
    logger.info(s"pythonLoadCode: ${loadCode}")
    loadCode
  }

}

// 加载Python的Python模块
class PythonEngineHook extends PythonModuleLoadEngineConnHook {

  // 设置engineType属性为"python"，表示此挂钩适用于python引擎
  override val engineType: String = "python"

  // 设置runType属性为RunType.PYTHON，表示此挂钩将执行python类型的代码
  override protected val runType: RunType = RunType.PYTHON

  // 重写constructCode方法，用于根据Python模块信息构造加载模块的代码
  override protected def constructCode(pythonModuleInfo: PythonModuleInfoVO): String = {
    // 处理文件
    val path: String = pythonModuleInfo.getPath
    val engineCreationContext: EngineCreationContext =
      EngineConnManager.getEngineConnManager.getEngineConn.getEngineCreationContext
    val user: String = engineCreationContext.getUser

    var loadCode: String = null
    logger.info(s"gen code in constructCode")
    Utils.tryAndWarn({
      // 获取引擎临时目录
      var tmpDir: String = EngineConnConf.getEngineTmpDir
      if (!tmpDir.endsWith("/")) {
        tmpDir += "/"
      }
      val fileName: String = new java.io.File(path).getName
      val destPath: String = tmpDir + fileName
      val config: Configuration = HDFSUtils.getConfiguration(HadoopConf.HADOOP_ROOT_USER.getValue)
      val fs: FileSystem = HDFSUtils.getHDFSUserFileSystem(user, config)
      fs.copyToLocalFile(new Path(path), new Path("file://" + destPath))
      if (fileName.endsWith("zip")) {
        tmpDir += fileName
      }
      loadCode = s"import sys; sys.path.append('${tmpDir}')"
      logger.info(s"5 load local python code: ${loadCode} in path: $destPath")
    })
    loadCode
  }

}
