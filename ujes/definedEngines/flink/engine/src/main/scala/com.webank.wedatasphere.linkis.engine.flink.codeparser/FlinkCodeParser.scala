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

package com.webank.wedatasphere.linkis.engine.flink.codeparser


import com.webank.wedatasphere.linkis.engine.execute.CodeType.CodeType
import com.webank.wedatasphere.linkis.engine.execute.{CodeType, EngineExecutorContext, SingleCodeParser}
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer

/**
 * Created by liangqilang on 01 20, 2021
 */
 class FlinkCodeParser extends SingleCodeParser{

  override val codeType: CodeType = CodeType.Flink
  val separator = ";"

  override def parse(code: String, engineExecutorContext: EngineExecutorContext): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]()
    def appendStatement(sqlStatement: String): Unit ={
      codeBuffer.append(sqlStatement)
    }
    if (StringUtils.contains(code, separator)) code.split(";\n").foreach{
      case s if StringUtils.isBlank(s) =>
      case s => appendStatement(s);
    } else code match {
      case s if StringUtils.isBlank(s) =>
      case s => appendStatement(s);
    }
    codeBuffer.toArray
  }

}
