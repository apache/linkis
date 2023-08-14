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

package org.apache.linkis.errorcode.common

import scala.util.matching.Regex

class LinkisErrorCode extends AbstractErrorCode {

  private var errorCode: String = _
  private var errorDesc: String = _
  private var errorRegex: Regex = _
  private var errorRegexStr: String = _
  private var errorType: Int = 0

  def this(errorCode: String, errorDesc: String) = {
    this()
    this.errorCode = errorCode
    this.errorDesc = errorDesc
  }

  def this(errorCode: String, errorDesc: String, errorRegexStr: String, errorType: Int) = {
    this()
    this.errorCode = errorCode
    this.errorDesc = errorDesc
    this.errorType = errorType
    this.errorRegexStr = errorRegexStr
    this.errorRegex = errorRegexStr.r.unanchored
  }

  override def getErrorCode: String = this.errorCode

  override def getErrorDesc: String = this.errorDesc

  override def getErrorRegex: Regex = this.errorRegex

  def setErrorCode(errorCode: String): Unit = this.errorCode = errorCode

  def setErrorDesc(errorDesc: String): Unit = this.errorDesc = errorDesc

  def setErrorRegex(errorRegex: Regex): Unit = this.errorRegex = errorRegex

  def setErrorType(errorType: Integer): Unit = this.errorType = errorType

  def getErrorType: Int = this.errorType

  def setErrorRegexStr(errorRegexStr: String): Unit = {
    this.errorRegexStr = errorRegexStr
    // logger.info("error reg str is {}", errorRegexStr)
    this.errorRegex = errorRegexStr.r.unanchored
  }

  override def getErrorRegexStr: String = this.errorRegexStr

  override def toString: String = {
    "错误码:" + this.errorCode + "," + "错误描述:" + this.errorDesc
  }

  override def hashCode(): Int = if (errorCode != null) errorCode.hashCode else super.hashCode()

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[LinkisErrorCode]) return false
    obj.asInstanceOf[LinkisErrorCode].getErrorCode.equals(this.errorCode)
  }

}
