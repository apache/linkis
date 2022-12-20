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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.entrance.conf.EntranceConfiguration

import org.junit.jupiter.api.{Assertions, Test}

class TestShellDangerousGrammerInterceptor {

  private val shellDangerCode = EntranceConfiguration.SHELL_DANGER_USAGE.getValue

  @Test
  def testShellDangerCode: Unit = {
    val shellDangerousGrammerInterceptor = new ShellDangerousGrammerInterceptor

    Assertions.assertEquals(
      "rm,sh,find,kill,python,for,source,hdfs,hadoop,spark-sql,spark-submit,pyspark,spark-shell,hive,yarn",
      shellDangerCode
    )
    Assertions.assertTrue(shellDangerousGrammerInterceptor.shellContainDangerUsage("rm"))
    Assertions.assertFalse(shellDangerousGrammerInterceptor.shellContainDangerUsage("ls"))

  }

}
