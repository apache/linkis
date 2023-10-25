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

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class ScalaCodeParserTest {

  @Test
  @DisplayName("parseTest")
  def parseTest(): Unit = {

    val scalaCode =
      "val codeBuffer = new ArrayBuffer[String]()\n    val statementBuffer = new ArrayBuffer[String]()"
    val scalaCodeParser = new ScalaCodeParser
    val array = scalaCodeParser.parse(scalaCode)
    Assertions.assertTrue(array.size == 3)

  }

  @Test
  @DisplayName("parseAbnormalCodeTest")
  def parseAbnormalCodeTest(): Unit = {

    val abnormalCode =
      "   def addInt( a:Int, b:Int )\n      var sum:Int = 0\n      sum = a + b\n      return sum\n   }"
    val scalaCodeParser = new ScalaCodeParser
    val array = scalaCodeParser.parse(abnormalCode)
    Assertions.assertTrue(array.length == 3)

  }

  @Test
  @DisplayName("parseImportCodeTest")
  def parseImportCodeTest(): Unit = {

    val importCode = "import java.io._\nobject Test {\n   def main(args: Array[String]) {\n      " +
      "val writer = new PrintWriter(new File(\"test.txt\" ))\n      writer.write(\"菜鸟教程\")\n      writer.close()\n   }\n}"

    val scalaCodeParser = new ScalaCodeParser
    val array = scalaCodeParser.parse(importCode)
    Assertions.assertTrue(array.length == 4)

  }

  @Test
  @DisplayName("parseSpecialCharCodeTest")
  def parseSpecialCharCodeTest(): Unit = {

    val specialCodeExp1 = "def sum(args: Int*) : Int = {\n    var result = 0 ;\n    for(s2 <- args) {\n      result += s2 ;\n    }\n    result ;\n}\n\n" +
      "def main(args: Array[String]): Unit = {\n    val s = sum(1 to 5:_*)     \n    println(s)\n}"

    val scalaCodeParser = new ScalaCodeParser
    val arrayResult1 = scalaCodeParser.parse(specialCodeExp1)

    Assertions.assertTrue(arrayResult1.length == 4)

    val specialCodeExp2 =
      "  @BeanProperty\n  var id: Long = _\n  @BeanProperty\n  var status: Int = 0\n  " +
        "@BeanProperty\n  var msg: String = _\n  @BeanProperty\n  var exception: Exception = _\n  " +
        "@BeanProperty\n  var data: util.Map[String, Object] = new util.HashMap[String, Object]()\n  " +
        "override def equals(o: Any): Boolean = {\n    if (this == o) return true\n    if (o == null || (getClass != o.getClass)) return false\n    " +
        "val that = o.asInstanceOf[JobRespProtocol]\n    new EqualsBuilder()\n      .append(status, that.status)\n      .append(msg, that.msg)\n      .append(exception, that.exception)\n      " +
        ".append(data, that.data)\n      .isEquals\n  }"

    val arrayResult2 = scalaCodeParser.parse(specialCodeExp2)
    Assertions.assertTrue(arrayResult2.length == 3)

  }

}
