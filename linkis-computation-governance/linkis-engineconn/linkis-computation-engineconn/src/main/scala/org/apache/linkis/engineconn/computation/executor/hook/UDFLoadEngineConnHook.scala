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

import org.apache.linkis.bml.client.{BmlClient, BmlClientFactory}
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, EngineTypeLabel, RunType}
import org.apache.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.linkis.udf.UDFClient
import org.apache.linkis.udf.utils.ConstantVar
import org.apache.linkis.udf.vo.UDFInfoVo

import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.charset.StandardCharsets

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.ArrayBuffer

abstract class UDFLoad extends Logging {

  protected val udfType: BigInt
  protected val category: String
  protected val runType: RunType

  private val bmlClient: BmlClient = BmlClientFactory.createBmlClient()

  protected def getRealRunType(engineType: String): RunType = runType

  protected def constructCode(udfInfo: UDFInfoVo): String

  protected def generateCode(): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]
    val statementBuffer = new ArrayBuffer[String]
    var accept = true
    getLoadUdfCode.split("\n").foreach {
      case "" =>
      case l if l.startsWith("%") =>
        if (acceptCodeType(l)) {
          accept = true
          codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
        } else {
          accept = false
        }
      case l if accept => statementBuffer.append(l)
      case _ =>
    }
    if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
    codeBuffer.filter(StringUtils.isNotBlank).toArray
  }

  protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%" + runType.toString)
  }

  protected def getLoadUdfCode: String = {
    val engineCreationContext =
      EngineConnManager.getEngineConnManager.getEngineConn.getEngineCreationContext
    val user = engineCreationContext.getUser
    val udfAllLoad =
      engineCreationContext.getOptions.getOrDefault("linkis.user.udf.all.load", "true").toBoolean
    val udfIdStr = engineCreationContext.getOptions.getOrDefault("linkis.user.udf.custom.ids", "")
    val udfIds = udfIdStr.split(",").filter(StringUtils.isNotBlank).map(s => s.toLong)

    logger.info(
      s"start loading UDFs, user: $user, load all: $udfAllLoad, udfIds: ${udfIds.mkString("Array(", ", ", ")")}"
    )

    val udfInfos = if (udfAllLoad) {
      UDFClient.getUdfInfosByUdfType(user, category, udfType)
    } else {
      UDFClient.getUdfInfosByUdfIds(user, udfIds, category, udfType)
    }
    logger.info("all udfs: ")

    udfInfos.foreach { l =>
      logger.info(
        s"udfName:${l.getUdfName}, bml_resource_id:${l.getBmlResourceId}, bml_id:${l.getId}\n"
      )
    }
    udfInfos
      .filter { info => StringUtils.isNotEmpty(info.getBmlResourceId) }
      .map(constructCode)
      .mkString("\n")
  }

  protected def readFile(path: String): String = {
    logger.info("read file: " + path)
    val file = new File(path)
    if (file.exists()) {
      FileUtils.readFileToString(file, StandardCharsets.UTF_8)
    } else {
      logger.info("udf file: [" + path + "] doesn't exist, ignore it.")
      ""
    }
  }

  protected def readFile(user: String, resourceId: String, resourceVersion: String): String = {
    logger.info("begin to download udf from bml.")
    val downloadResponse = bmlClient.downloadResource(
      if (user == null) Utils.getJvmUser else user,
      resourceId,
      resourceVersion
    )
    if (downloadResponse.isSuccess) {
      Utils.tryFinally {
        IOUtils.toString(downloadResponse.inputStream, Configuration.BDP_ENCODING.getValue)
      } {
        IOUtils.closeQuietly(downloadResponse.inputStream)
      }
    } else {
      logger.info("failed to download udf from bml.")
      ""
    }
  }

  private def getFunctionCode: Array[String] = Utils.tryCatch(generateCode()) { t: Throwable =>
    if (!ComputationExecutorConf.UDF_LOAD_FAILED_IGNORE.getValue) {
      logger.error("Failed to load function, executor close ")
      throw t
    } else {
      logger.error("Failed to load function", t)
      Array.empty[String]
    }
  }

  private def executeFunctionCode(codes: Array[String], executor: ComputationExecutor): Unit = {
    if (null == codes || null == executor) {
      return
    }
    codes.foreach { code =>
      logger.info("Submit function registration to engine, code: " + code)
      Utils.tryCatch(executor.executeLine(new EngineExecutionContext(executor), code)) {
        t: Throwable =>
          if (!ComputationExecutorConf.UDF_LOAD_FAILED_IGNORE.getValue) {
            Utils.tryQuietly(executor.close())
            logger.error("Failed to load function, executor close ")
            throw t
          } else {
            logger.error("Failed to load function", t)
            null
          }
      }
    }
  }

  protected def loadFunctions(executor: Executor): Unit = {

    val codes = getFunctionCode
    if (null != codes && codes.nonEmpty) {
      executor match {
        case computationExecutor: ComputationExecutor =>
          executeFunctionCode(codes, computationExecutor)
        case _ =>
      }
    }
    logger.info(s"Successful to execute function code ${runType}, type : ${udfType}")
  }

  protected def loadUDF(labels: Array[Label[_]]): Unit = {

    val codes = getFunctionCode
    if (null != codes && codes.nonEmpty) {
      val executor = ExecutorManager.getInstance.getExecutorByLabels(labels)
      executor match {
        case computationExecutor: ComputationExecutor =>
          executeFunctionCode(codes, computationExecutor)
        case _ =>
      }
    }
    logger.info(s"Successful to execute code ${runType}, type : ${udfType}")
  }

}

abstract class UDFLoadEngineConnHook extends UDFLoad with EngineConnHook with Logging {

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    val codeLanguageLabel = new CodeLanguageLabel
    engineCreationContext.getLabels().asScala.find(_.isInstanceOf[EngineTypeLabel]) match {
      case Some(engineTypeLabel) =>
        codeLanguageLabel.setCodeType(
          getRealRunType(engineTypeLabel.asInstanceOf[EngineTypeLabel].getEngineType).toString
        )
      case None =>
        codeLanguageLabel.setCodeType(runType.toString)
        logger.warn("no EngineTypeLabel found, use default runType")
    }
    val labels = Array[Label[_]](codeLanguageLabel)
    loadUDF(labels)
  }

  override def afterEngineServerStartFailed(
      engineCreationContext: EngineCreationContext,
      throwable: Throwable
  ): Unit = {}

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {}

}

class JarUdfEngineHook extends UDFLoadEngineConnHook {
  override val udfType: BigInt = ConstantVar.UDF_JAR
  override val category: String = ConstantVar.UDF
  override val runType = RunType.SQL

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    "%sql\n" + udfInfo.getRegisterFormat
  }

  override protected def getRealRunType(engineType: String): RunType = {
    if (engineType.equals("hive")) {
      return RunType.HIVE
    }
    runType
  }

}

class PyUdfEngineHook extends UDFLoadEngineConnHook {
  override val udfType: BigInt = ConstantVar.UDF_PY
  override val category: String = ConstantVar.UDF
  override val runType = RunType.PYSPARK

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    "%py\n" + readFile(
      udfInfo.getCreateUser,
      udfInfo.getBmlResourceId,
      udfInfo.getBmlResourceVersion
    ) + "\n" +
      (if (StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }

}

class ScalaUdfEngineHook extends UDFLoadEngineConnHook {
  override val udfType: BigInt = ConstantVar.UDF_SCALA
  override val category: String = ConstantVar.UDF
  override val runType = RunType.SCALA

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    "%scala\n" + readFile(
      udfInfo.getCreateUser,
      udfInfo.getBmlResourceId,
      udfInfo.getBmlResourceVersion
    ) + "\n" +
      (if (StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }

}
