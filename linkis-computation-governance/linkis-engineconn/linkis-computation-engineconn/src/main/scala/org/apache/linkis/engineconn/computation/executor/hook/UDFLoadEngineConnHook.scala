/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.computation.executor.hook

import java.io.File

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, EngineTypeLabel}
import org.apache.linkis.udf.UDFClient
import org.apache.linkis.udf.entity.UDFInfo
import org.apache.linkis.udf.utils.ConstantVar
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

abstract class UDFLoadEngineConnHook extends EngineConnHook with Logging {
  protected val udfType: BigInt
  protected val category: String
  protected val runType: String

  protected def getRealRunType(engineType: String): String = runType

  protected def constructCode(udfInfo: UDFInfo): String

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    val user = engineCreationContext.getUser
    val codeLanguageLabel = new CodeLanguageLabel
    engineCreationContext.getLabels().find(_.isInstanceOf[EngineTypeLabel]) match {
      case Some(engineTypeLabel) =>
        codeLanguageLabel.setCodeType(getRealRunType(engineTypeLabel.asInstanceOf[EngineTypeLabel].getEngineType))
      case None =>
        codeLanguageLabel.setCodeType(runType)
        warn("no EngineTypeLabel found, use default runType")
    }
    val labels = Array[Label[_]](codeLanguageLabel)
    generateCode(user).foreach {
      case "" =>
      case c: String =>
        info("Submit udf registration to engine, code: " + c)
        ExecutorManager.getInstance.getExecutorByLabels(labels) match {
          case executor: ComputationExecutor =>
//            val task  = new CommonEngineConnTask("udf-register-" + UDFLoadEngineConnHook.taskIdGenerator.incrementAndGet(), false)
//            task.setCode(c)
//            task.setStatus(ExecutionNodeStatus.Scheduled)
//            task.setLabels(labels)
//            task.setProperties(Maps.newHashMap())
//            executor.execute(task)
            Utils.tryCatch(executor.executeLine(new EngineExecutionContext(executor), c)){
              case t: Throwable =>
                if (! ComputationExecutorConf.UDF_LOAD_FAILED_IGNORE.getValue) {
                  Utils.tryQuietly(executor.close())
                  throw t
                }else {
                  error("Failed to load udf", t)
                  null
                }
            }
        }
        info("executed code: " + c)
    }
  }

  protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%" + runType)
  }

  protected def generateCode(user: String): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]
    val statementBuffer = new ArrayBuffer[String]
    var accept = true
    getLoadUdfCode(user).split("\n").foreach{
      case "" =>
      case l if l.startsWith("%") =>
        if(acceptCodeType(l)){
          accept = true
          codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
        }else{
          accept = false
        }
      case l if accept => statementBuffer.append(l)
      case _ =>
    }
    if(statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
    codeBuffer.toArray
  }

  protected def getLoadUdfCode(user: String): String = {
    info("start loading UDFs")
    val udfInfos = UDFClient.getUdfInfos(user, category).filter{ info => info.getUdfType == udfType && info.getExpire == false && info.getLoad == true}
    udfInfos.map(constructCode).mkString("\n")
  }

  protected def readFile(path: String): String = {
    info("read file: " + path)
    val file = new File(path)
    if(file.exists()){
      FileUtils.readFileToString(file)
    } else {
      info("udf file: [" + path + "] doesn't exist, ignore it.")
      ""
    }
  }

  override def afterEngineServerStartFailed(engineCreationContext: EngineCreationContext, throwable: Throwable): Unit = {}

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

}

class JarUdfEngineHook extends UDFLoadEngineConnHook {
  override val udfType: BigInt = ConstantVar.UDF_JAR
  override val category: String = ConstantVar.UDF
  override val runType = "sql"

  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%sql\n" + udfInfo.getRegisterFormat
  }

  override protected def getRealRunType(engineType: String): String = {
    if(engineType.equals("hive")){
      return "hql"
    }
    runType
  }
}
class PyUdfEngineHook extends UDFLoadEngineConnHook{
  override val udfType: BigInt = ConstantVar.UDF_PY
  override val category: String = ConstantVar.UDF
  override val runType = "py"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%py\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
class ScalaUdfEngineHook extends UDFLoadEngineConnHook{
  override val udfType: BigInt = ConstantVar.UDF_SCALA
  override val category: String = ConstantVar.UDF
  override val runType = "scala"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
class PyFunctionEngineHook extends UDFLoadEngineConnHook{
  override val udfType: BigInt = ConstantVar.FUNCTION_PY
  override val category: String = ConstantVar.FUNCTION
  override val runType = "py"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%py\n" + readFile(udfInfo.getPath)
  }

  override protected def getRealRunType(engineType: String): String = {
    if(engineType.equals("python")){
      return "python"
    }
    runType
  }
}
class ScalaFunctionEngineHook extends UDFLoadEngineConnHook{
  override val udfType: BigInt = ConstantVar.FUNCTION_SCALA
  override val category: String = ConstantVar.FUNCTION
  override val runType = "scala"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath)
  }
}

//object UDFLoadEngineConnHook {
//  val taskIdGenerator = new AtomicInteger(0)
//}
