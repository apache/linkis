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

package org.apache.linkis.engineconnplugin.flink.setting

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.flink.context.{EnvironmentContext, FlinkEngineConnContext}

/**
 * Used to set extra configurations when launching a flink EngineConn. These configurations is not
 * needed as common, but it is very useful in some special scenes.
 */
trait Settings {

  def setEnvironmentContext(
      engineCreationContext: EngineCreationContext,
      context: EnvironmentContext
  ): Unit

  def setExecutionContext(
      engineCreationContext: EngineCreationContext,
      context: FlinkEngineConnContext
  ): Unit

}
