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

package org.apache.linkis.engineplugin.server.localize

import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration
import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration.ENGINE_CONN_HOME
import org.apache.linkis.engineplugin.server.localize.EngineConnBmlResourceGenerator.NO_VERSION_MARK
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary._
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.storage.io.IOClient.logger

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.file.Paths
import java.text.MessageFormat

abstract class AbstractEngineConnBmlResourceGenerator extends EngineConnBmlResourceGenerator {

  if (!new File(getEngineConnsHome).exists) {
    throw new EngineConnPluginErrorException(
      CANNOT_HOME_PATH_EC.getErrorCode,
      CANNOT_HOME_PATH_EC.getErrorDesc
    )
  }

  protected def getEngineConnsHome: String = {
    ENGINE_CONN_HOME.getValue
  }

  protected def getEngineConnDistHome(engineConnTypeLabel: EngineTypeLabel): String =
    getEngineConnDistHome(engineConnTypeLabel.getEngineType, engineConnTypeLabel.getVersion)

  protected def getEngineConnDistHome(engineConnType: String, version: String): String = {
    val engineConnDistHome = Paths.get(getEngineConnsHome, engineConnType, "dist").toFile.getPath
    checkEngineConnDistHome(engineConnDistHome)
    if (StringUtils.isBlank(version) || NO_VERSION_MARK == version) return engineConnDistHome
    val engineConnPackageHome = Paths.get(engineConnDistHome, version).toFile.getPath
    logger.info("getEngineConnDistHome, engineConnPackageHome path:" + engineConnPackageHome)
    val engineConnPackageHomeFile = new File(engineConnPackageHome)

    if (!engineConnPackageHomeFile.exists()) {
      if (
          !version.startsWith(
            "v"
          ) && EngineConnPluginConfiguration.EC_BML_VERSION_MAY_WITH_PREFIX_V.getValue
      ) {
        val versionOld = "v" + version
        val engineConnPackageHomeOld = Paths.get(engineConnDistHome, versionOld).toFile.getPath
        logger.info(
          "try to getEngineConnDistHome with prefix v, engineConnPackageHome path:" + engineConnPackageHomeOld
        )
        val engineConnPackageHomeFileOld = new File(engineConnPackageHomeOld)
        if (!engineConnPackageHomeFileOld.exists()) {
          throw new EngineConnPluginErrorException(
            ENGINE_VERSION_NOT_FOUND.getErrorCode,
            MessageFormat.format(ENGINE_VERSION_NOT_FOUND.getErrorDesc, version, engineConnType)
          )
        } else {
          engineConnPackageHomeOld
        }
      } else {
        throw new EngineConnPluginErrorException(
          ENGINE_VERSION_NOT_FOUND.getErrorCode,
          MessageFormat.format(ENGINE_VERSION_NOT_FOUND.getErrorDesc, version, engineConnType)
        )
      }
    } else {
      engineConnPackageHome
    }

  }

  private def checkEngineConnDistHome(engineConnPackageHomePath: String): Unit = {
    val engineConnPackageHomeFile = new File(engineConnPackageHomePath)
    checkEngineConnDistHome(engineConnPackageHomeFile)
  }

  private def checkEngineConnDistHome(engineConnPackageHome: File): Unit = {
    if (!engineConnPackageHome.exists()) {
      throw new EngineConnPluginErrorException(
        CANNOT_HOME_PATH_DIST.getErrorCode,
        CANNOT_HOME_PATH_DIST.getErrorDesc
      )
    }
  }

  protected def getEngineConnDistHomeList(engineConnType: String): Array[String] = {
    val engineConnDistHome = Paths.get(getEngineConnsHome, engineConnType, "dist").toFile.getPath
    val engineConnDistHomeFile = new File(engineConnDistHome)
    checkEngineConnDistHome(engineConnDistHomeFile)
    val children = engineConnDistHomeFile.listFiles()
    if (children.isEmpty) {
      throw new EngineConnPluginErrorException(
        DIST_IS_EMPTY.getErrorCode,
        MessageFormat.format(DIST_IS_EMPTY.getErrorDesc, engineConnType)
      )
    } else {
      children.map(_.getPath)
    }
  }

  def getEngineConnTypeListFromDisk: Array[String] =
    new File(getEngineConnsHome).listFiles().map(_.getName)

}
