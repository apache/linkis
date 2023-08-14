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

package org.apache.linkis.ecm.server.hook

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.server.service.LocalDirsHandleService
import org.apache.linkis.ecm.server.service.impl.DefaultLocalDirsHandleService
import org.apache.linkis.ecm.server.util.ECMUtils
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.{
  Environment,
  ProcessEngineConnLaunchRequest
}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageUtils}
import org.apache.linkis.udf.UDFClient

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.file.Paths

import scala.collection.mutable

class JarUDFLoadECMHook extends ECMHook with Logging {

  private var localDirsHandleService: LocalDirsHandleService = new DefaultLocalDirsHandleService

  private val fs: FileSystem = FSFactory.getFs(StorageUtils.FILE).asInstanceOf[FileSystem]

  fs.init(null)

  override def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn): Unit = {
    request match {
      case pel: ProcessEngineConnLaunchRequest =>
        logger.info("start loading UDFs")
        val user = pel.user
        val ticketId = pel.ticketId

        val udfAllLoad =
          pel.creationDesc.properties.getOrDefault("linkis.user.udf.all.load", "true").toBoolean
        val udfIdStr = pel.creationDesc.properties.getOrDefault("linkis.user.udf.custom.ids", "")
        val udfIds = udfIdStr.split(",").filter(StringUtils.isNotBlank).map(s => s.toLong)

        val engineType = LabelUtil.getEngineType(pel.labels)
        val workDir = localDirsHandleService.getEngineConnWorkDir(user, ticketId, engineType)
        val pubDir = localDirsHandleService.getEngineConnPublicDir
        val udfDir = workDir + File.separator + "udf"
        val fsPath = new FsPath(StorageUtils.FILE_SCHEMA + udfDir)
        if (!fs.exists(fsPath)) {
          FileSystemUtils.mkdirs(fs, fsPath, Utils.getJvmUser)
          fs.setPermission(fsPath, "rwxrwxrwx")
        }

        val udfInfos =
          if (udfAllLoad) UDFClient.getJarUdf(pel.user)
          else UDFClient.getJarUdfByIds(pel.user, udfIds)
        val fileNameSet: mutable.HashSet[String] = new mutable.HashSet[String]()
        import util.control.Breaks._
        udfInfos.foreach { udfInfo =>
          val resourceId = udfInfo.getBmlResourceId
          val version = udfInfo.getBmlResourceVersion
          val bmlResourceDir =
            StorageUtils.FILE_SCHEMA + Paths.get(pubDir, resourceId, version).toFile.getPath
          val fsPath = new FsPath(bmlResourceDir)
          val bmlResource: BmlResource = new BmlResource
          bmlResource.setResourceId(resourceId)
          bmlResource.setVersion(version)
          // 文件名用path最后一截
          val fileName = udfInfo.getPath.substring(udfInfo.getPath.lastIndexOf("/") + 1)
          bmlResource.setFileName(fileName)
          breakable {
            if (StringUtils.isEmpty(fileName) || fileNameSet.contains(fileName)) {
              break()
            }
            fileNameSet += bmlResource.getFileName
            if (!fs.exists(fsPath)) resourceId.intern().synchronized {
              if (!fs.exists(fsPath)) {
                ECMUtils.downLoadBmlResourceToLocal(
                  bmlResource,
                  if (udfInfo.getCreateUser.equals("bdp")) "hadoop" else udfInfo.getCreateUser,
                  fsPath.getPath
                )(fs)
                logger.info(s"Finished to download bml resource ${fsPath.getPath}")
              }
            }
            conn.getEngineConnLaunchRunner.getEngineConnLaunch
              .getEngineConnManagerEnv()
              .linkDirs
              .put(
                fsPath.getPath + File.separator + bmlResource.getFileName,
                udfDir + File.separator + bmlResource.getFileName
              )
          }
        }
        val udfJars = fileNameSet.map(udfDir + File.separator + _)
        pel.environment.put(Environment.UDF_JARS.toString, udfJars.mkString(","));
    }
  }

  override def afterLaunch(conn: EngineConn): Unit = {}

  override def getName: String = "JarUDFLoadECMHook"
}
