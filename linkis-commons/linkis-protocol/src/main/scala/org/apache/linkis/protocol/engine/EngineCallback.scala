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

package org.apache.linkis.protocol.engine

object EngineCallback {
  private val DWC_APPLICATION_NAME = "dwc.application.name"
  private val DWC_INSTANCE = "dwc.application.instance"

  def mapToEngineCallback(options: Map[String, String]): EngineCallback =
    EngineCallback(options(DWC_APPLICATION_NAME), options(DWC_INSTANCE))

  def callbackToMap(engineCallback: EngineCallback): Map[String, String] =
    Map(
      DWC_APPLICATION_NAME -> engineCallback.applicationName,
      DWC_INSTANCE -> engineCallback.instance
    )

}

case class EngineCallback(applicationName: String, instance: String)
