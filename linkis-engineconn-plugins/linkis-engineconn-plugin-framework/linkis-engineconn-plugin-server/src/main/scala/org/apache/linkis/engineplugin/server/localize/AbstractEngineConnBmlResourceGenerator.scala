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
 
package org.apache.linkis.engineplugin.server.localize

import java.io.File
import java.nio.file.Paths

import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration.ENGINE_CONN_HOME
import org.apache.linkis.engineplugin.server.localize.EngineConnBmlResourceGenerator.NO_VERSION_MARK
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils


abstract class AbstractEngineConnBmlResourceGenerator extends EngineConnBmlResourceGenerator {

  if(!new File(getEngineConnsHome).exists)
    throw new EngineConnPluginErrorException(20001, s"Cannot find the home path(${getEngineConnsHome}) of engineConn.")

  protected def getEngineConnsHome: String = {
    ENGINE_CONN_HOME.getValue
  }

  protected def getEngineConnDistHome(engineConnTypeLabel: EngineTypeLabel): String =
    getEngineConnDistHome(engineConnTypeLabel.getEngineType, engineConnTypeLabel.getVersion)

  protected def getEngineConnDistHome(engineConnType: String, version: String): String = {
    val engineConnDistHome = Paths.get(getEngineConnsHome, engineConnType, "dist").toFile.getPath
    if(!new File(engineConnDistHome).exists())
      throw new EngineConnPluginErrorException(20001, "Cannot find the home path of engineconn dist.")
    if(StringUtils.isBlank(version) || NO_VERSION_MARK == version) return engineConnDistHome
    val engineConnPackageHome = Paths.get(engineConnDistHome, "v" + version).toFile.getPath
    if(new File(engineConnPackageHome).exists()) engineConnPackageHome
    else engineConnDistHome
  }

  protected def getEngineConnDistHomeList(engineConnType: String): Array[String] = {
    val engineConnDistHome = Paths.get(getEngineConnsHome, engineConnType, "dist").toFile.getPath
    val engineConnDistHomeFile = new File(engineConnDistHome)
    if(!engineConnDistHomeFile.exists())
      throw new EngineConnPluginErrorException(20001, "Cannot find the home path of engineconn dist.")
    val children = engineConnDistHomeFile.listFiles()
    if(children.isEmpty)
      throw new EngineConnPluginErrorException(20001, s"The dist of ${engineConnType}EngineConn is empty.")
    else if(!children.exists(_.getName.startsWith("v"))) {
      Array(engineConnDistHome)
    } else if(children.forall(_.getName.startsWith("v"))) children.map(_.getPath)
    else throw new EngineConnPluginErrorException(20001, s"The dist of ${engineConnType}EngineConn is irregular, both the version dir and non-version dir are exist.")
  }

  def getEngineConnTypeListFromDisk: Array[String] = new File(getEngineConnsHome).listFiles().map(_.getName)

}
