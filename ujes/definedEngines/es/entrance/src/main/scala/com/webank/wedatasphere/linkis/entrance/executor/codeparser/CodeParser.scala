/**
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
package com.webank.wedatasphere.linkis.entrance.executor.codeparser

import com.webank.wedatasphere.linkis.entrance.conf.EsEntranceConfiguration
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
trait CodeParser {

  def parse(code: String): Array[String]

}

class EsJsonCodeParser extends CodeParser {

  def parse(code: String): Array[String] = {
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

class EsSQLCodeParser extends CodeParser {

  val SQL_FORMAT = EsEntranceConfiguration.ES_SQL_FORMAT.getValue

  override def parse(code: String): Array[String] = {
    // sql to request body
    parseSql(code).map(item => item.trim.replaceAll("\\s+", " ")).map(String.format(SQL_FORMAT, _))
  }

  val separator = ";"
  val defaultLimit:Int = EsEntranceConfiguration.ENGINE_DEFAULT_LIMIT.getValue
  def parseSql(code: String): Array[String] = {
    //val realCode = StringUtils.substringAfter(code, "\n")
    val codeBuffer = new ArrayBuffer[String]()
    def appendStatement(sqlStatement: String): Unit ={
      codeBuffer.append(sqlStatement)
    }
    if (StringUtils.contains(code, separator)) {
      StringUtils.split(code, ";").foreach{
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s + " limit " + defaultLimit);
        case s => appendStatement(s);
      }
    } else {
      code match {
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s + " limit " + defaultLimit);
        case s => appendStatement(s);
      }
    }
    codeBuffer.toArray
  }

  def isSelectCmdNoLimit(cmd: String): Boolean = {
    var code = cmd.trim
    if(!cmd.split("\\s+")(0).equalsIgnoreCase("select")) return false
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

object CodeParser {

  val ESSQL_CODE_PARSER = new EsSQLCodeParser
  val ESJSON_CODE_PARSER = new EsJsonCodeParser

}
