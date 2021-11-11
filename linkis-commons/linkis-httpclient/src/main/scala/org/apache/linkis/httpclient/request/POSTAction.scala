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
 
package org.apache.linkis.httpclient.request

import java.util


abstract class POSTAction extends GetAction {
  private val formParams: util.Map[String, String] = new util.HashMap[String, String]
  private val payload: util.Map[String, Any] = new util.HashMap[String, Any]

  def setFormParam(key: String, value: Any): Unit = if(value != null) this.formParams.put(key, value.toString)
  def getFormParams: util.Map[String, String] = formParams

  def addRequestPayload(key: String, value: Any): Unit = if(value != null) this.payload.put(key, value)
  def getRequestPayloads = payload

  def getRequestPayload: String

}