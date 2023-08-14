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

package org.apache.linkis.computation.client.once.result

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.ujes.client.exception.UJESJobException

import java.util

@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/executeEngineConnOperation")
class EngineConnOperateResult extends LinkisManagerResult {

  private var result: util.Map[String, Any] = _
  private var errorMsg: String = _
  private var isError: Boolean = _

  def setResult(result: util.Map[String, Any]): Unit = {
    this.result = result
  }

  def getErrorMsg(): String = errorMsg

  def setErrorMsg(errorMsg: String): Unit = this.errorMsg = errorMsg

  def getIsError(): Boolean = isError

  def setIsError(isError: Boolean): Unit = this.isError = isError

  def getResult: util.Map[String, Any] =
    if (isError) throw new UJESJobException(20301, errorMsg) else result

  def getAsOption[T](key: String): Option[T] = Option(getAs(key))

  def getAs[T](key: String): T = getAs[T](key, null.asInstanceOf[T])

  def getAs[T](key: String, defaultValue: T): T =
    if (getResult != null && result.containsKey(key)) result.get(key).asInstanceOf[T]
    else defaultValue

}
