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

import org.apache.linkis.common.utils.{Logging, Utils, ZipUtils}
import org.apache.linkis.engineplugin.server.localize.EngineConnBmlResourceGenerator.NO_VERSION_MARK
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary._

import org.apache.commons.lang3.StringUtils

import java.io.{File, FileInputStream, InputStream}
import java.text.MessageFormat

class DefaultEngineConnBmlResourceGenerator
    extends AbstractEngineConnBmlResourceGenerator
    with Logging {

  override def generate(engineConnType: String): Map[String, Array[EngineConnLocalizeResource]] =
    getEngineConnDistHomeList(engineConnType).map { path =>
      val versionFile = new File(path)
      logger.info("generate, versionFile:" + path)
      val key = versionFile.getName
      if (key.contains("-")) {
        throw new EngineConnPluginErrorException(
          CONTAINS_SPECIAL_CHARCATERS.getErrorCode,
          MessageFormat.format(CONTAINS_SPECIAL_CHARCATERS.getErrorDesc, engineConnType)
        )
      }
      Utils.tryCatch {
        key -> generateDir(versionFile.getPath)
      } { case t: Throwable =>
        logger.error(s"Generate dir : $path error, msg : " + t.getMessage, t)
        throw t
      }
    }.toMap

  override def generate(
      engineConnType: String,
      version: String
  ): Array[EngineConnLocalizeResource] = {
    val path = getEngineConnDistHome(engineConnType, version)
    generateDir(path)
  }

  private def generateDir(path: String): Array[EngineConnLocalizeResource] = {
    val distFile = new File(path)
    logger.info("generateDir, distFile:" + path)
    val validFiles = distFile
      .listFiles()
      .filterNot(f =>
        f.getName.endsWith(".zip") &&
          new File(path, f.getName.replace(".zip", "")).exists
      )
    validFiles.map { file =>
      if (file.isFile) {
        EngineConnLocalizeResourceImpl(
          file.getPath,
          file.getName,
          file.lastModified(),
          file.length()
        )
          .asInstanceOf[EngineConnLocalizeResource]
      } else {
        val newFile = new File(path, file.getName + ".zip")
        if (newFile.exists() && !newFile.delete()) {
          throw new EngineConnPluginErrorException(
            NO_PERMISSION_FILE.getErrorCode,
            MessageFormat.format(NO_PERMISSION_FILE.getErrorDesc, newFile)
          )
        }
        ZipUtils.fileToZip(file.getPath, path, file.getName + ".zip")
        // If it is a folder, the last update time here is the last update time of the folder, not the last update time of ZIP.(如果是文件夹，这里的最后更新时间，采用文件夹的最后更新时间，而不是ZIP的最后更新时间.)
        EngineConnLocalizeResourceImpl(
          newFile.getPath,
          newFile.getName,
          file.lastModified(),
          newFile.length()
        )
          .asInstanceOf[EngineConnLocalizeResource]
      }
    }
  }

}

case class EngineConnLocalizeResourceImpl(
    filePath: String,
    fileName: String,
    lastModified: Long,
    fileSize: Long
) extends EngineConnLocalizeResource {
  override def getFileInputStream: InputStream = new FileInputStream(filePath)
}
