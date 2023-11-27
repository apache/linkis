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

package org.apache.linkis.ecm.core.utils

import org.apache.linkis.common.utils.{Logging, Utils}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.{BufferedReader, FileReader, IOException}
import java.net.ServerSocket
import java.util.Properties

object PortUtils extends Logging {

  /**
   * portRange: '-' is the separator
   *
   * @return
   */
  def findAvailPortByRange(portRange: String): Int = {
    val separator = "-"
    if (StringUtils.isEmpty(portRange) || portRange.equals(separator)) {
      this.findAvailPort()
    } else {
      // valid user registered port range: 1024-65535
      // refer https://en.wikipedia.org/wiki/Registered_port
      var (start, end) = (1024, 65535)
      val ports = portRange.split(separator, -1)
      if (!ports(0).isEmpty) start = ports(0).toInt
      if (!ports(1).isEmpty) end = ports(1).toInt
      val availablePort = start to end find { port =>
        try {
          new ServerSocket(port).close()
          true
        } catch {
          case ioe: IOException => false
        }
      }
      availablePort.getOrElse(
        throw new IOException("No available port in the portRange: " + portRange)
      )
    }
  }

  def findAvailPort(): Int = {
    val socket = new ServerSocket(0)
    Utils.tryFinally(socket.getLocalPort)(IOUtils.closeQuietly(socket))
  }

  def readFromProperties(propertiesFile: String): Properties = {
    val properties: Properties = new Properties
    var reader: BufferedReader = null;
    try {
      reader = new BufferedReader(new FileReader(propertiesFile))
      properties.load(reader)
    } catch {
      case e: Exception =>
        logger.warn(s"loading vsersion faild with path $propertiesFile  error:$e")
    } finally {
      try if (reader != null) reader.close
      catch {
        case e: Exception =>
          logger.warn(s"try to close buffered reader with error:${e.getMessage}")
      }
    }
    properties
  }

}
