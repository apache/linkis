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

package org.apache.linkis.storage.script.parser

import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER
import org.apache.linkis.storage.exception.StorageErrorException
import org.apache.linkis.storage.script.{Parser, Variable, VariableParser}

abstract class CommonScriptParser extends Parser {

  @scala.throws[StorageErrorException]
  def parse(line: String): Variable = {
    val variableReg = ("\\s*" + prefix + "\\s*(.+)\\s*" + "=" + "\\s*(.+)\\s*").r
    line match {
      case variableReg(key, value) =>
        Variable(VariableParser.VARIABLE, null, key.trim, value.trim)
      case _ =>
        val split = line.split("=")
        if (split.length != 2) {
          throw new StorageErrorException(
            INVALID_CUSTOM_PARAMETER.getErrorCode(),
            INVALID_CUSTOM_PARAMETER.getErrorDesc
          )
        }
        val value = split(1).trim
        val subSplit = split(0).split(" ")
        if (subSplit.filter(_ != "").size != 4) {
          throw new StorageErrorException(
            INVALID_CUSTOM_PARAMETER.getErrorCode(),
            INVALID_CUSTOM_PARAMETER.getErrorDesc
          )
        }
        if (!subSplit.filter(_ != "")(0).equals(prefixConf)) {
          throw new StorageErrorException(
            INVALID_CUSTOM_PARAMETER.getErrorCode(),
            INVALID_CUSTOM_PARAMETER.getErrorDesc
          )
        }
        val sortParent = subSplit.filter(_ != "")(1).trim
        val sort = subSplit.filter(_ != "")(2).trim
        val key = subSplit.filter(_ != "")(3).trim
        Variable(sortParent, sort, key, value)
    }
  }

  override def getAnnotationSymbol(): String = prefix.split('@')(0)

}
