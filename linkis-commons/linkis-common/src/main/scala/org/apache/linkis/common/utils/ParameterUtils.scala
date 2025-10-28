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

package org.apache.linkis.common.utils

object ParameterUtils {

  private val startupConfRegex =
    """--([a-z]+)-conf\s+(\S+)=([^=]+?)(?=\s*(?:--engineconn-conf|--spring-conf|$))""".r

  def parseStartupParams(args: Array[String], handler: (String, String, String) => Unit): Unit = {
    val argString = args.mkString(" ")
    startupConfRegex.findAllMatchIn(argString).foreach { m =>
      val prefix = m.group(1).trim
      val key = m.group(2).trim
      val value = m.group(3).trim
      prefix match {
        case "engineconn" | "spring" =>
          handler(prefix, key, value)
        case _ =>
          throw new IllegalArgumentException(s"illegal command line, $prefix cannot recognize.")
      }
    }
  }

}
