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
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration._
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineHook}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, RunTypeExecuteRequest}
import com.webank.wedatasphere.linkis.server.JMap
import com.webank.wedatasphere.linkis.udf.api.rpc.{RequestUdfTree, ResponseUdfTree}
import com.webank.wedatasphere.linkis.udf.entity.{UDFInfo, UDFTree}
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class UdfLoadEngineHook extends EngineHook with Logging{ self =>
  protected val udfType: BigInt
  protected val category: String
  protected val runType: String
  protected var creator: String = _
  protected var user: String = _
  protected var initSpecialCode: String = _

  protected def constructCode(udfInfo: UDFInfo): String

  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    creator = params.get("creator")
    user = params.get("user")
    initSpecialCode = getLoadUdfCode()
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

  protected def acceptCodeType(line: String): Boolean = {
    line.startsWith("%" + runType)
  }

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

  protected def getLoadUdfCode(): String = {
    info("start loading UDFs")
    val udfInfos = extractUdfInfos().filter{info => info.getUdfType == udfType && info.getExpire == false && info.getLoad == true}
    udfInfos.map(constructCode).mkString("\n")
  }

  protected def extractUdfInfos(): mutable.ArrayBuffer[UDFInfo] = {
    val udfInfoBuilder = new mutable.ArrayBuffer[UDFInfo]
    val udfTree = queryUdfRpc(user)
    extractUdfInfos(udfInfoBuilder, udfTree, user)
    udfInfoBuilder
  }

  protected def extractUdfInfos(udfInfoBuilder: mutable.ArrayBuffer[UDFInfo], udfTree: UDFTree, userName: String) : Unit = {
    if(CollectionUtils.isNotEmpty(udfTree.getUdfInfos)){
      for(udfInfo <- udfTree.getUdfInfos){
        udfInfoBuilder.append(udfInfo)
      }
    }
    if(CollectionUtils.isNotEmpty(udfTree.getChildrens)){
      for(child <- udfTree.getChildrens){
        var childInfo = child
        if(TreeType.specialTypes.contains(child.getUserName)){
          childInfo = queryUdfRpc(userName, child.getId, child.getUserName)
        } else {
          childInfo = queryUdfRpc(userName, child.getId, TreeType.SELF)
        }
        extractUdfInfos(udfInfoBuilder, childInfo, userName)
      }
    }
  }

  private def queryUdfRpc(userName: String, treeId: Long = -1, treeType: String = "self"): UDFTree = {
    val udfTree = Sender.getSender(ENGINE_UDF_APP_NAME.getValue)
      .ask(RequestUdfTree(userName, treeType, treeId, category))
      .asInstanceOf[ResponseUdfTree]
      .udfTree
    //info("got udf tree:" + new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(udfTree))
    udfTree
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


class JarUdfEngineHook extends UdfLoadEngineHook{
  override val udfType: BigInt = UdfType.UDF_JAR
  override val category: String = UdfCategory.UDF
  override val runType = "sql"

  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%sql\n" + udfInfo.getRegisterFormat
  }
}
class PyUdfEngineHook extends UdfLoadEngineHook{
  override val udfType: BigInt = UdfType.UDF_PY
  override val category: String = UdfCategory.UDF
  override val runType = "python"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%python\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
class ScalaUdfEngineHook extends UdfLoadEngineHook{
  override val udfType: BigInt = UdfType.UDF_SCALA
  override val category: String = UdfCategory.UDF
  override val runType = "scala"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
class PyFunctionEngineHook extends UdfLoadEngineHook{
  override val udfType: BigInt = UdfType.FUNCTION_PY
  override val category: String = UdfCategory.FUNCTION
  override val runType = "python"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%python\n" + readFile(udfInfo.getPath)
  }
}
class ScalaFunctionEngineHook extends UdfLoadEngineHook{
  override val udfType: BigInt = UdfType.FUNCTION_SCALA
  override val category: String = UdfCategory.FUNCTION
  override val runType = "scala"
  override protected def constructCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath)
  }
}

object UdfType {
  val UDF_JAR = 0
  val UDF_PY = 1
  val UDF_SCALA = 2
  val FUNCTION_PY = 3
  val FUNCTION_SCALA = 4
}
object UdfCategory{
  val UDF = "udf"
  val FUNCTION = "function"
}
object TreeType {
  val SYS = "sys"
  val BDP = "bdp"
  val SELF = "self"
  val SHARE = "share"
  def specialTypes = Array(SYS, BDP, SHARE)
}
