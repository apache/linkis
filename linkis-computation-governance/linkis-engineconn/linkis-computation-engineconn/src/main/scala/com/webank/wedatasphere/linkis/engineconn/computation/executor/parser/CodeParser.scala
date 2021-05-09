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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.parser

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.parser.CodeType.CodeType
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


trait CodeParser {

  def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String]

}

abstract class SingleCodeParser extends CodeParser {
  val codeType: CodeType

  def canParse(codeType: String): Boolean = {
    CodeType.getType(codeType) == this.codeType
  }
}

abstract class CombinedEngineCodeParser extends CodeParser {
  val parsers: Array[SingleCodeParser]

  def getCodeType(code: String, engineExecutorContext: EngineExecutionContext): String

  override def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String] = {
    val codeType = getCodeType(code, engineExecutorContext)
    parsers.find(_.canParse(codeType)) match {
      case Some(parser) => parser.parse(code, engineExecutorContext)
      case None => Array(code)
    }
  }
}

class ScalaCodeParser extends SingleCodeParser with Logging {

  override val codeType: CodeType = CodeType.Scala

  override def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String] = {
    //val realCode = StringUtils.substringAfter(code, "\n")
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[String]()
    code.split("\n").foreach {
      case "" =>
      case l if l.startsWith(" ") || l.startsWith("\t") => if (!l.trim.startsWith("//")) statementBuffer.append(l)
      case l if l.startsWith("@") => statementBuffer.append(l)
      case l if StringUtils.isNotBlank(l) =>
        if (l.trim.startsWith("}")) {
          statementBuffer.append(l)
        } else {
          if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
          //statementBuffer.append("%scala")
          statementBuffer.append(l)
        }
      case _ =>
    }
    if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
    codeBuffer.toArray
  }
}

class PythonCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.Python
  val openBrackets = Array("{", "(", "[")
  val closeBrackets = Array("}", ")", "]")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  override def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String] = {
    //val realCode = StringUtils.substringAfter(code, "\n")
    val bracketStack = new mutable.Stack[String]
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[String]()
    var notDoc = true
    //quotationMarks is used to optimize the three quotes problem（quotationMarks用来优化三引号问题）
    var quotationMarks: Boolean = false
    code.split("\n").foreach {
      case "" =>
      case l if l.trim.contains("\"\"\"") || l.trim.contains("""'''""") => quotationMarks = !quotationMarks
        statementBuffer.append(l)
        recordBrackets(bracketStack, l)
      case l if quotationMarks => statementBuffer.append(l)
      //shanhuang 用于修复python的引号问题
      //recordBrackets(bracketStack, l)
      case l if notDoc && l.startsWith("#") =>
      case l if StringUtils.isNotBlank(statementBuffer.last) && statementBuffer.last.endsWith("""\""") =>
        statementBuffer.append(l)
      case l if notDoc && l.startsWith(" ") =>
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("\t") =>
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("@") =>
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("else") => //LOG.info("I am else")
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("elif") => //LOG.info("I am elif")
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && StringUtils.isNotBlank(l) =>
        if (statementBuffer.nonEmpty && bracketStack.isEmpty) {
          codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
        }
        // statementBuffer.append("%python")
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case _ =>
    }
    if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
    codeBuffer.toArray
  }

  def recordBrackets(bracketStack: mutable.Stack[String], l: String): Unit = {
    val real = l.replace("\"\"\"", "").replace("'''", "").trim
    if (StringUtils.endsWithAny(real, openBrackets)) {
      for (i <- (0 to real.length - 1).reverse) {
        val token = real.substring(i, i + 1)
        if (openBrackets.contains(token)) {
          bracketStack.push(token)
        }
      }
    }
    if (StringUtils.startsWithAny(real, closeBrackets)) {
      for (i <- 0 to real.length - 1) {
        val token = real.substring(i, i + 1)
        if (closeBrackets.contains(token)) {
          bracketStack.pop()
        }
      }
    }
  }

}


object Main {
  def main(args: Array[String]): Unit = {
    val codeParser = new PythonCodeParser
    val code = "if True: \n print 1 \nelif N=123: \n print 456 \nelse: \n print 789"
    println(code)
    val arrCodes = codeParser.parse(code, null)
    print(arrCodes.mkString("||\n"))
  }
}

class SQLCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.SQL

  val separator = ";"

  val defaultLimit: Int = ComputationExecutorConf.ENGINE_DEFAULT_LIMIT.getValue

  override def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String] = {
    //val realCode = StringUtils.substringAfter(code, "\n")
    val codeBuffer = new ArrayBuffer[String]()

    def appendStatement(sqlStatement: String): Unit = {
      codeBuffer.append(sqlStatement)
    }

    if (StringUtils.contains(code, separator)) {
      StringUtils.split(code, ";").foreach {
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s);
        case s => appendStatement(s);
      }
    } else {
      code match {
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s);
        case s => appendStatement(s);
      }
    }
    codeBuffer.toArray
  }

  def isSelectCmdNoLimit(cmd: String): Boolean = {
    var code = cmd.trim
    if (!cmd.split("\\s+")(0).equalsIgnoreCase("select")) return false
    if (code.contains("limit")) code = code.substring(code.lastIndexOf("limit")).trim
    else if (code.contains("LIMIT")) code = code.substring(code.lastIndexOf("LIMIT")).trim.toLowerCase
    else return true
    val hasLimit = code.matches("limit\\s+\\d+\\s*;?")
    if (hasLimit) {
      if (code.indexOf(";") > 0) code = code.substring(5, code.length - 1).trim
      else code = code.substring(5).trim
      val limitNum = code.toInt
      if (limitNum > defaultLimit) throw new IllegalArgumentException("We at most allowed to limit " + defaultLimit + ", but your SQL has been over the max rows.")
    }
    !hasLimit
  }
}

class JsonCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.JSON

  override def parse(code: String, engineExecutorContext: EngineExecutionContext): Array[String] = {
    // parse json code
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[Char]()

    var status = 0
    var isBegin = false
    code.trim.toCharArray().foreach {
      case '{' => {
        if (status == 0) {
          if (isBegin && !statementBuffer.isEmpty) {
            codeBuffer.append(new String(statementBuffer.toArray))
            statementBuffer.clear()
          } else {
            isBegin = true
          }
        }
        status -= 1
        statementBuffer.append('{')
      }
      case '}' => {
        status += 1
        statementBuffer.append('}')
      }
      case char: Char => if (status == 0 && isBegin && !statementBuffer.isEmpty) {
        codeBuffer.append(new String(statementBuffer.toArray))
        statementBuffer.clear()
        isBegin = false
      } else {
        statementBuffer.append(char)
      }
    }

    if(statementBuffer.nonEmpty) codeBuffer.append(new String(statementBuffer.toArray))

    codeBuffer.toArray
  }

}

object CodeType extends Enumeration {
  type CodeType = Value
  val Python, SQL, Scala, Shell, JSON, Other = Value

  def getType(codeType: String): CodeType = codeType.toLowerCase() match {
    case "python" | "pyspark" | "py" => Python
    case "sql" | "hql" | "essql" => SQL
    case "scala" => Scala
    case "shell" => Shell
    case "esjson" => JSON
    case _ => Other
  }
}

