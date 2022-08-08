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
package org.apache.linkis.engineplugin.trino.utils

import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer


object SqlCodeParser {
  val separator = ";"
  val subCol = "SELECT \"SUBCOL\" AS \"COL\" FROM ( SELECT 1 AS \"SUBCOL\" ) \"SUBQUERY\" GROUP BY \"COL\""
  private val grantPattern = "(?=.*grant)(?=.*on)(?=.*to)^.*$"

  def parse(code: String): String = {
    val codeBuffer = new ArrayBuffer[String]()

    def appendStatement(sqlStatement: String): Unit = {
      codeBuffer.append(sqlStatement)
    }

    if (StringUtils.contains(code, separator)) {
      StringUtils.split(code, ";").foreach {
        case s if StringUtils.isBlank(s) =>
        case s => appendStatement(s.replaceAll("`", "\""));
      }
    } else {
      code match {
        case s if StringUtils.isBlank(s) =>
        case s =>
          val pattern = """`[a-zA-Z_0-9 ]+`""".r.unanchored
          var tmpS = s
          pattern.findAllIn(s).foreach(a => {
            val s1 = a.replaceAll("\\s*", "")
            tmpS = tmpS.replace(a, s1)
          })
          appendStatement(tmpS.replaceAll("`", "\""));
      }
    }

    if (codeBuffer.size == 1) {
      var code = codeBuffer(0)
      code = code.trim.replaceAll("\n", " ").replaceAll("\\s+", " ")
      if (code.contains(subCol)) {
        codeBuffer(0) = "SELECT 1"
      }
    }

    codeBuffer.toArray.head
  }

  def checkGrant(code: String): Boolean = {
    code.matches(grantPattern)
  }

  def checkModifySchema(code: String): Boolean = {
    code.trim.replaceAll("\n", " ").replaceAll("\\s+", " ")
      .toLowerCase().contains("create schema") || code.contains("drop schema") || code.contains("alter schema")
  }
}
