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

package org.apache.linkis.manager.engineplugin.common.resource

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.message.RequestProtocol

import java.util

trait EngineResourceRequest {
  val user: String
  val labels: util.List[Label[_]]
  val properties: util.Map[String, String]
}

trait TimeoutResourceRequest {
  val timeout: Long
}

case class NormalEngineResourceRequest(
    user: String,
    labels: util.List[Label[_]],
    properties: util.Map[String, String]
) extends EngineResourceRequest

case class TimeoutEngineResourceRequest(
    timeout: Long,
    user: String,
    labels: util.List[Label[_]],
    properties: util.Map[String, String]
) extends EngineResourceRequest
    with TimeoutResourceRequest
