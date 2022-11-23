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

package org.apache.linkis.engineconnplugin.seatunnel.util

import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelSparkEnvConfiguration

import org.apache.commons.io.IOUtils
import org.apache.commons.logging.{Log, LogFactory}

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}

object SeatunnelUtils {
  val LOGGER: Log = LogFactory.getLog(SeatunnelUtils.getClass)
  private var process: Process = _

  def localArray(code: String): Array[String] = {
    Array(SeatunnelSparkEnvConfiguration.GET_LINKIS_SPARK_CONFIG, generateExecFile(code))
  }

  def generateExecFile(code: String): String = {
    val file = new File(
      System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue) + "/config_" + System
        .currentTimeMillis()
    )
    val writer = new PrintWriter(file)
    writer.write(code)
    writer.close()
    file.getAbsolutePath
  }

  def executeLine(code: String): Int = {
    var bufferedReader: BufferedReader = null
    try {
      val processBuilder: ProcessBuilder = new ProcessBuilder(generateRunCode(code): _*)
      process = processBuilder.start()
      bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream))
      var line: String = null
      while ({
        line = bufferedReader.readLine(); line != null
      }) {
        LOGGER.info(line)
      }
      val exitcode = process.waitFor()
      exitcode
    } finally {
      IOUtils.closeQuietly(bufferedReader)
    }
  }

  private def generateRunCode(code: String): Array[String] = {
    Array("sh", "-c", code)
  }

}
