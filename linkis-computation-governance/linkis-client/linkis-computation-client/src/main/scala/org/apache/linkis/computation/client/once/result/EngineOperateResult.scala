/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.computation.client.once.result

import java.util

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/executeEngineOperation")
class EngineOperateResult extends LinkisManagerResult {

  private var result: util.Map[String, Any] = _

  def setResult(result: util.Map[String, Any]): Unit = {
    this.result = result
  }

  def getResult: util.Map[String, Any] = result

  def getAs[T](key: String): Option[T] = {
    if (result != null && result.get(key) != null) {
      Some(result.get(key).asInstanceOf[T])
    } else {
      None
    }
  }


}