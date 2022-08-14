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

package org.apache.linkis.entrance.log

import org.apache.linkis.errorcode.client.handler.LinkisErrorCodeHandler

abstract class ErrorCodeManager {

  def getErrorCodes: Array[ErrorCode]

  def errorMatch(log: String): Option[(String, String)] = {
    getErrorCodes.foreach(e =>
      if (e.regex.findFirstIn(log).isDefined) {
        val matched = e.regex.unapplySeq(log)
        if (matched.nonEmpty) {
          return Some(e.code -> e.message.format(matched.get: _*))
        } else Some(e.code -> e.message)
      }
    )
    None
  }

}

/**
 * this error code is from errorcode server
 */
object FlexibleErrorCodeManager extends ErrorCodeManager {

  private val errorCodeHandler = LinkisErrorCodeHandler.getInstance()

  override def getErrorCodes: Array[ErrorCode] = Array.empty

  override def errorMatch(log: String): Option[(String, String)] = {
    val errorCodes = errorCodeHandler.handle(log)
    if (errorCodes != null && errorCodes.size() > 0) {
      Some(errorCodes.get(0).getErrorCode, errorCodes.get(0).getErrorDesc)
    } else {
      None
    }
  }

}

object Main {
  def main(args: Array[String]): Unit = {}
}
