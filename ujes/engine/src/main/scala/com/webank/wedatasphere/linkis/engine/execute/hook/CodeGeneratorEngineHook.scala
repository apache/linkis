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

package com.webank.wedatasphere.linkis.engine.execute.hook

import java.io.File

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineHook}
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, RunTypeExecuteRequest}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by enjoyyin on 2018/9/27.
  */
@Deprecated
//changed to UdfLoadEngineHook
abstract class CodeGeneratorEngineHook extends EngineHook with Logging{ self =>
  val udfPathProp = "udf.paths"
  protected var creator: String = _
  protected var user: String = _
  protected var initSpecialCode: String = _
  protected val runType: String

  protected def acceptCodeType(line: String): Boolean

  protected def generateCode(): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]
    val statementBuffer = new ArrayBuffer[String]
    var accept = true
    initSpecialCode.split("\n").foreach{
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

  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    creator = params.get("creator")
    user = params.get("user")
    initSpecialCode = StringUtils.split(params.get(udfPathProp), ",").map(readFile).mkString("\n")
    params
  }

  override def afterCreatedEngine(executor: EngineExecutor): Unit = {
    generateCode().foreach {
      case "" =>
      case c: String =>
        info("Submit udf registration to engine, code: " + c)
        executor.execute(new ExecuteRequest with RunTypeExecuteRequest{
          override val code: String = c
          override val runType: String = self.runType
        })
        info("executed code: " + c)
    }
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
}
@Deprecated
class SqlCodeGeneratorEngineHook extends CodeGeneratorEngineHook{
  override val runType = "sql"
  override protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%sql")
  }
}
@Deprecated
class PythonCodeGeneratorEngineHook extends CodeGeneratorEngineHook{
  override val runType = "python"
  override protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%python")
  }
}
@Deprecated
class ScalaCodeGeneratorEngineHook extends CodeGeneratorEngineHook{
  override val runType = "scala"
  override protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%scala")
  }
}