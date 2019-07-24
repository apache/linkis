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

package com.webank.wedatasphere.linkis.storage.script.parser

import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.script.{Parser, Variable}


/**
  * Created by johnnwang on 2018/10/23.
  */
abstract class CommonScriptParser extends Parser {

  override def prefixConf: String = "conf@set"

  @scala.throws[StorageErrorException]
  def parse(line: String): Variable = {
    val variableReg = ("\\s*" + prefix + "\\s*(.+)\\s*" + "=" + "\\s*(.+)\\s*").r
    val configReg = ("\\s*" + prefixConf + "\\s*(.+)\\s*(.+)\\s*" + "=" + "\\s*(.+)\\s*").r
    line match {
      case variableReg(key, value) => new Variable("variable", null, key.trim, value.trim)
      case configReg(sort,key,value) => new Variable("configuration",sort.trim,key.trim,value.trim)
      case _ => throw new StorageErrorException(65000, "Invalid custom parameter(不合法的自定义参数)")
    }
  }
}

