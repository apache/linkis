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

package org.apache.linkis.engineplugin.spark.common

import org.apache.commons.lang3.StringUtils

/**
 */
trait Kind

object Kind {

  val RESTART_CODE = "@restart"

  val APPLICATION_START_COMMAND = List(
    "\\s*@restart\\s*",
    "\\s*[@|%][a-zA-Z]{1,12}\\s*",
    "^\\s*@set\\s*spark\\..+\\s*",
    "^\\s*#.+\\s*",
    "^\\s*//.+\\s*",
    "^\\s*--.+\\s*",
    "\\s*"
  )

  def needToRestart(code: String): Boolean = code.startsWith(RESTART_CODE)

  private def getIndex(_code: String, start: Int): Int = {
    val index1 = _code.indexOf("\n", start)
    val index2 = _code.indexOf("\\n", start)
    if (index1 > -1 && index2 > -1) Math.min(index1, index2) else Math.max(index1, index2)
  }

  def getKindString(code: String): String = {
    val _code = StringUtils.strip(code)

    var start = 0
    if (_code.startsWith(RESTART_CODE)) {
      start = getIndex(_code, 0) + 1
    }
    var index = getIndex(_code, start)
    if (index == -1) index = _code.length
    StringUtils.strip(_code.substring(start, index))
  }

  def getKind(code: String): String = {
    val kindStr = getKindString(code)
    if (kindStr.matches("[%|@][a-zA-Z]{1,12}")) kindStr.substring(1)
    else throw new IllegalArgumentException("Unknown kind " + kindStr)
  }

  /**
   * This method just removes @restart and language identifiers (such as %sql, %scala, etc.), that
   * is, removes at most the first 2 rows. 该方法只是去掉了@restart和语言标识符（如%sql、%scala等），即最多只去掉最前面2行
   *
   * @param code
   * @return
   */
  def getRealCode(code: String): String = {
    val _code = StringUtils.strip(code)
    val kindStr = getKindString(_code)
    if (kindStr.matches("[%|@][a-zA-Z]{1,12}")) {
      StringUtils.strip(_code.substring(_code.indexOf(kindStr) + kindStr.length))
    } else if (_code.startsWith(RESTART_CODE)) {
      StringUtils.strip(_code.substring(RESTART_CODE.length))
    } else _code
  }

  /**
   * This method removes all code-independent setting parameters and identifiers (including
   * comments) 该方法去掉一切与代码无关的设置参数和标识符（包括注释）
   *
   * @param code
   * @return
   */
  def getFormatCode(code: String): String = {
    val msg = new StringBuilder
    val restartRegex = "\\s*@restart\\s*".r
    val kindRegex = "\\s*[@|%][a-zA-Z]{1,12}\\s*".r
    val setRegex = "^\\s*@set\\s*.+\\s*".r
    val symbolRegex1 = "^\\s*#.+\\s*".r
    val symbolRegex2 = "^\\s*//.+\\s*".r
    val symbolRegex3 = "\\s*--.+\\s*".r
    val blankRegex = "\\s*".r
    code.split("\n").foreach {
      case blankRegex() | setRegex() | symbolRegex1() | symbolRegex2() | symbolRegex3() |
          restartRegex() | kindRegex() =>
      case str => msg ++= str ++ "\n"
    }
    StringUtils.strip(msg.toString)
  }

}
