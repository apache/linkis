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
 
package org.apache.linkis.protocol.engine

import org.apache.linkis.protocol.RetryableProtocol
import org.apache.linkis.protocol.message.RequestProtocol


case class RequestEngineStatus(messageType: Int) extends RetryableProtocol with RequestProtocol
object RequestEngineStatus {
  val Status_Only = 1
  val Status_Overload = 2
  val Status_Concurrent = 3
  val Status_Overload_Concurrent = 4
  val Status_BasicInfo = 5
  val ALL = 6
}