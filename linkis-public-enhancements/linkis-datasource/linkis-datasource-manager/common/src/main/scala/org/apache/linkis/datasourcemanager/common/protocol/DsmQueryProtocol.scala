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

package org.apache.linkis.datasourcemanager.common.protocol

import org.apache.linkis.protocol.message.RequestProtocol

import java.util

/**
 * Store error code map
 */
trait DsmQueryProtocol extends RequestProtocol {}

/**
 * Query request of Data Source Information
 * @param id
 */
case class DsInfoQueryRequest(id: String, name: String, system: String) extends DsmQueryProtocol {

  def isValid: Boolean = {
    (Option(id).isDefined || Option(name).isDefined) && Option(system).isDefined
  }

}

/**
 * Response of parameter map
 * @param params
 */
case class DsInfoResponse(
    status: Boolean,
    dsType: String = "",
    params: util.Map[String, Object] = new util.HashMap[String, Object](),
    creator: String = "",
    errorMsg: String = ""
) extends DsmQueryProtocol
