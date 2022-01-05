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
 
package org.apache.linkis.governance.common.paser

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.paser.CodeType.CodeType
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import java.util


trait CodeParser {

  def parse(code: String): Array[String]

}

object  CodeParserFactory {

  val defaultCodeParsers: util.Map[CodeType, CodeParser] = new util.HashMap[CodeType, CodeParser]()

  def getCodeParser(codeType: CodeType): CodeParser = {
    if (null == defaultCodeParsers || defaultCodeParsers.isEmpty) {
      initCodeParsers()
    }
    defaultCodeParsers.getOrDefault(codeType, null)
  }

  def initCodeParsers(): Unit = {
    defaultCodeParsers.put(CodeType.SQL, new SQLCodeParser)
    if (GovernanceCommonConf.PYTHON_CODE_PARSER_SWITCH.getValue) {
      defaultCodeParsers.put(CodeType.Python, new PythonCodeParser)
    }
    if (GovernanceCommonConf.SCALA_CODE_PARSER_SWITCH.getValue){
      defaultCodeParsers.put(CodeType.Scala, new ScalaCodeParser)
    }
//    defaultCodeParsers.put(CodeType.Other, new EmptyCodeParser)
  }

}

abstract class SingleCodeParser extends CodeParser {
  val codeType: CodeType

  def canParse(codeType: String): Boolean = {
    CodeType.getType(codeType) == this.codeType
  }
}

abstract class CombinedEngineCodeParser extends CodeParser {
  val parsers: Array[SingleCodeParser]

  def getCodeType(code: String): String

  override def parse(code: String): Array[String] = {
    val codeType = getCodeType(code)
    parsers.find(_.canParse(codeType)) match {
      case Some(parser) => parser.parse(code)
      case None => Array(code)
    }
  }
}

class ScalaCodeParser extends SingleCodeParser with Logging {

  override val codeType: CodeType = CodeType.Scala

  override def parse(code: String): Array[String] = {
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

  override def parse(code: String): Array[String] = {
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
      //用于修复python的引号问题
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


class SQLCodeParser extends SingleCodeParser with Logging  {

  override val codeType: CodeType = CodeType.SQL

  val separator = ";"

  val specialSeparator = """\;"""

  val defaultLimit: Int = GovernanceCommonConf.ENGINE_DEFAULT_LIMIT.getValue

  private def findRealSemicolonIndex(tempCode: String):Array[Int] = {
    val realTempCode = if (!tempCode.endsWith(""";""")) tempCode + ";" else tempCode
    val array = new ArrayBuffer[Int]()
    for(i <- 0 until realTempCode.length - 1){
      if ('\\' != realTempCode.charAt(i) && ';' == realTempCode.charAt(i + 1)) array += ( i + 1)
    }
    array.toArray
  }

  override def parse(code: String): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]()
    def appendStatement(sqlStatement: String): Unit = {
      codeBuffer.append(sqlStatement)
    }
    if (StringUtils.contains(code, specialSeparator)) {
      val indices = findRealSemicolonIndex(code)
      var oldIndex = 0
      indices.foreach{
        index => val singleCode = code.substring(oldIndex, index)
          oldIndex = index + 1
          if(StringUtils.isNotBlank(singleCode)) appendStatement(singleCode)
      }
    } else if (StringUtils.contains(code, separator)) {
      StringUtils.split(code, ";").foreach{
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
      if (limitNum > defaultLimit) // throw new IllegalArgumentException("We at most allowed to limit " + defaultLimit + ", but your SQL has been over the max rows.")
        warn(s"Limit num ${limitNum} is over then max rows : ${defaultLimit}")
    }
    !hasLimit
  }
}

class EmptyCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.Other

  override def parse(code: String): Array[String] = Array(code)

}


object CodeType extends Enumeration {
  type CodeType = Value
  val Python, SQL, Scala, Shell, Other, Remain, JSON = Value

  def getType(codeType: String): CodeType = codeType.toLowerCase() match {
    case "python" | "pyspark" | "py" => Python
    case "sql" | "hql" | "psql" => SQL
    case "scala" => Scala
    case "shell" | "sh" => Shell
    case _ => Other
  }
}

