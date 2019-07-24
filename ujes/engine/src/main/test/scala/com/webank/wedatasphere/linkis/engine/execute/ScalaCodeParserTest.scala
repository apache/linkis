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

package com.webank.wedatasphere.linkis.engine.execute

import java.io.File

import com.google.common.io.Resources
import org.apache.commons.io.FileUtils

object ScalaCodeParserTest {
  def main(args: Array[String]): Unit = {
    val parser = new ScalaCodeParser
    var code = FileUtils.readFileToString(new File(Resources.getResource("test.scala.txt").getPath))
    parser.parse(code, null).foreach { statement =>
      println("---------------------------statement begin-----------------")
      println(statement)
      println("---------------------------statement end-----------------")
    }
  }

}
