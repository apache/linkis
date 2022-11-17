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

package org.apache.linkis.engineconnplugin.seatunnel.launch

import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelEnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.{
  variable,
  HADOOP_CONF_DIR
}
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants.addPathToClassPath

import org.apache.commons.lang3.StringUtils

import java.nio.file.Paths
import java.util

import scala.collection.JavaConverters._

class SeatunnelEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {}
