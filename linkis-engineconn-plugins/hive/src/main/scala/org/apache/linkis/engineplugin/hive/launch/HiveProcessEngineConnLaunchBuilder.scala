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

package org.apache.linkis.engineplugin.hive.launch

import org.apache.linkis.engineplugin.hive.conf.HiveEngineConfiguration.HIVE_ENGINE_CONN_JAVA_EXTRA_OPTS
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder

import org.apache.commons.lang3.StringUtils

import java.util

import com.google.common.collect.Lists

class HiveProcessEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def ifAddHiveConfigPath: Boolean = true

  override protected def getEngineConnManagerHooks(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[String] = {
    Lists.newArrayList("JarUDFLoadECMHook")
  }

  override protected def getExtractJavaOpts: String = {
    val hiveExtraOpts: String = HIVE_ENGINE_CONN_JAVA_EXTRA_OPTS.getValue
    if (StringUtils.isNotBlank(hiveExtraOpts)) {
      super.getExtractJavaOpts + " " + hiveExtraOpts
    } else {
      super.getExtractJavaOpts
    }
  }

}
