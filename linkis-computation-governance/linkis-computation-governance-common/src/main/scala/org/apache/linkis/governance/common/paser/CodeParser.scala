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

package org.apache.linkis.governance.common.paser

import org.apache.linkis.common.utils.{CodeAndRunTypeUtils, Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.paser.CodeType.CodeType

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Locale

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import org.slf4j.{Logger, LoggerFactory}

trait CodeParser {

  def parse(code: String): Array[String]

}

object CodeParserFactory {

  val defaultCodeParsers: util.Map[CodeType, CodeParser] =
    new util.HashMap[CodeType, CodeParser]()

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
    if (GovernanceCommonConf.SCALA_CODE_PARSER_SWITCH.getValue) {
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
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[String]()
    code.split("\n").foreach {
      case "" =>
      case l if l.startsWith(" ") || l.startsWith("\t") =>
        if (!l.trim.startsWith("//")) statementBuffer.append(l)
      case l if l.startsWith("@") => statementBuffer.append(l)
      case l if StringUtils.isNotBlank(l) =>
        if (l.trim.startsWith("}")) {
          statementBuffer.append(l)
        } else {
          if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
          statementBuffer.append(l)
        }
      case _ =>
    }
    if (statementBuffer.nonEmpty) codeBuffer.append(statementBuffer.mkString("\n"))

    // Append code `val linkisVar=1` in ends to prevent bugs that do not exit tasks for a long time
    if (GovernanceCommonConf.SCALA_PARSE_APPEND_CODE_ENABLED) {
      codeBuffer.append(GovernanceCommonConf.SCALA_PARSE_APPEND_CODE)
    }
    codeBuffer.toArray
  }

}

class PythonCodeParser extends SingleCodeParser with Logging {

  override val codeType: CodeType = CodeType.Python
  val openBrackets: Array[String] = Array("{", "(", "[")
  val closeBrackets: Array[String] = Array("}", ")", "]")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  override def parse(code: String): Array[String] = {
    if (GovernanceCommonConf.SKIP_PYTHON_PARSER.getValue) {
      return Array(code)
    }
    Utils.tryCatch(parsePythonCode(code)) { e =>
      logger.info(s"Your code will be submitted in overall mode. ${e.getMessage} ")
      Array(code)
    }
  }

  def parsePythonCode(code: String): Array[String] = {
    val bracketStack = new mutable.Stack[String]
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[String]()
    var notDoc = true
    // quotationMarks is used to optimize the three quotes problem
    var quotationMarks: Boolean = false
    code.split("\n").foreach {
      case "" =>
      case l if l.trim.contains("\"\"\"") || l.trim.contains("""'''""") =>
        quotationMarks = !quotationMarks
        statementBuffer.append(l)
        recordBrackets(bracketStack, l)
      case l if quotationMarks => statementBuffer.append(l)
      // quotationMarks is used to optimize the three quotes problem（quotationMarks用来优化三引号问题）
      // recordBrackets(bracketStack, l)
      case l if notDoc && l.startsWith("#") =>
      case l
          if statementBuffer.nonEmpty && StringUtils
            .isNotBlank(statementBuffer.last) && statementBuffer.last.endsWith("""\""") =>
        statementBuffer.append(l)
      case l if notDoc && l.startsWith(" ") =>
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("\t") =>
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("@") =>
        if (
            statementBuffer.nonEmpty && bracketStack.isEmpty && StringUtils
              .isNotBlank(statementBuffer.last) && !statementBuffer.last.startsWith("@")
        ) {
          codeBuffer.append(statementBuffer.mkString("\n"))
          statementBuffer.clear()
        }
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("else") => // LOG.info("I am else")
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && l.startsWith("elif") => // LOG.info("I am elif")
        statementBuffer.append(l)
        recordBrackets(bracketStack, l.trim)
      case l if notDoc && StringUtils.isNotBlank(l) =>
        if (
            statementBuffer.nonEmpty && bracketStack.isEmpty && StringUtils
              .isNotBlank(statementBuffer.last) && !statementBuffer.last.startsWith("@")
        ) {
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
    if (StringUtils.endsWithAny(real, openBrackets: _*)) {
      for (i <- (0 until real.length).reverse) {
        val token = real.substring(i, i + 1)
        if (openBrackets.contains(token)) {
          bracketStack.push(token)
        }
      }
    }
    if (StringUtils.startsWithAny(real, closeBrackets: _*)) {
      for (i <- 0 until real.length) {
        val token = real.substring(i, i + 1)
        if (closeBrackets.contains(token)) {
          bracketStack.pop()
        }
      }
    }
  }

}

class SQLCodeParser extends SingleCodeParser with Logging {

  override val codeType: CodeType = CodeType.SQL

  val separator = ";"

  val specialSeparator = """\;"""

  val defaultLimit: Int = GovernanceCommonConf.ENGINE_DEFAULT_LIMIT.getValue

  private def findRealSemicolonIndex(tempCode: String): Array[Int] = {
    var realTempCode = if (!tempCode.endsWith(""";""")) tempCode + ";" else tempCode
    //    replace \" or \' to  AA(only as placeholders ) .
    realTempCode = realTempCode.replace("""\"""", "AA").replace("""\'""", "AA")
    val re = """(['"])(?:(?!\1).)*?\1""".r
    val array = new ArrayBuffer[Int]()
    val uglyIndices = re.findAllMatchIn(realTempCode).map(m => (m.start, m.end)).toList
    for (i <- 0 until realTempCode.length) {
      if (';' == realTempCode.charAt(i) && uglyIndices.forall(se => i < se._1 || i >= se._2)) {
        array += i
      }
    }
    array.toArray
  }

  override def parse(code: String): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]()
    if (StringUtils.isBlank(code)) {
      return codeBuffer.toArray
    }

    def appendStatement(sqlStatement: String): Unit = {
      codeBuffer.append(sqlStatement)
    }
    val indices = findRealSemicolonIndex(code)
    var oldIndex = 0
    indices.foreach { index =>
      val singleCode = code.substring(oldIndex, index)
      oldIndex = index + 1
      if (StringUtils.isNotBlank(singleCode)) appendStatement(singleCode)
    }
    codeBuffer.toArray
  }

  def isSelectCmdNoLimit(cmd: String): Boolean = {
    var code = cmd.trim
    if (!cmd.split("\\s+")(0).equalsIgnoreCase("select")) return false
    if (code.contains("limit")) code = code.substring(code.lastIndexOf("limit")).trim
    else if (code.contains("LIMIT")) {
      code = code.substring(code.lastIndexOf("LIMIT")).trim.toLowerCase(Locale.getDefault)
    } else return true
    val hasLimit = code.matches("limit\\s+\\d+\\s*;?")
    if (hasLimit) {
      if (code.indexOf(";") > 0) code = code.substring(5, code.length - 1).trim
      else code = code.substring(5).trim
      val limitNum = code.toInt
      if (limitNum > defaultLimit) {
        // throw new IllegalArgumentException("We at most allowed to limit " + defaultLimit + ", but your SQL has been over the max rows.")
        logger.warn(s"Limit num ${limitNum} is over then max rows : ${defaultLimit}")
      }
    }
    !hasLimit
  }

}

class EmptyCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.Other

  override def parse(code: String): Array[String] = Array(code)

}

class JsonCodeParser extends SingleCodeParser {

  override val codeType: CodeType = CodeType.JSON

  override def parse(code: String): Array[String] = {
    // parse json code
    val codeBuffer = new ArrayBuffer[String]()
    val statementBuffer = new ArrayBuffer[Char]()

    var status = 0
    var isBegin = false
    code.trim.toCharArray().foreach {
      case '{' =>
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
      case '}' =>
        status += 1
        statementBuffer.append('}')
      case char: Char =>
        if (status == 0 && isBegin && !statementBuffer.isEmpty) {
          codeBuffer.append(new String(statementBuffer.toArray))
          statementBuffer.clear()
          isBegin = false
        } else {
          statementBuffer.append(char)
        }
    }

    if (statementBuffer.nonEmpty) codeBuffer.append(new String(statementBuffer.toArray))

    codeBuffer.toArray
  }

}

object CodeType extends Enumeration {
  type CodeType = Value
  val Python, SQL, Scala, Shell, Other, Remain, JSON = Value

  def getType(codeType: String): CodeType = {
    val languageTypeType = CodeAndRunTypeUtils.getLanguageTypeAndCodeTypeRelationMap(
      codeType.toLowerCase(Locale.getDefault)
    )
    languageTypeType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_PYTHON => Python
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL => SQL
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SCALA => Scala
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SHELL => Shell
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_JSON => JSON
      case _ => Other
    }
  }

}
