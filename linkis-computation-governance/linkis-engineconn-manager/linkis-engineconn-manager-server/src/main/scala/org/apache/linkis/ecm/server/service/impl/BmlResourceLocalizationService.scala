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

package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils, ZipUtils}
import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.launch.EngineConnManagerEnv
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary._
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.ecm.server.exception.ECMErrorException
import org.apache.linkis.ecm.server.service.{LocalDirsHandleService, ResourceLocalizationService}
import org.apache.linkis.ecm.server.util.ECMUtils
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.ProcessEngineConnLaunchRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageUtils}

import org.springframework.core.env.Environment

import java.io.File
import java.nio.file.Paths

import scala.collection.JavaConverters._
import scala.collection.mutable

class BmlResourceLocalizationService extends ResourceLocalizationService with Logging {

  private implicit val fs: FileSystem =
    FSFactory.getFs(StorageUtils.FILE).asInstanceOf[FileSystem]

  fs.init(null)

  private val seperator = File.separator

  private val schema = StorageUtils.FILE_SCHEMA

  private var localDirsHandleService: LocalDirsHandleService = _

  private var springEnv: Environment = _

  def setLocalDirsHandleService(localDirsHandleService: LocalDirsHandleService): Unit =
    this.localDirsHandleService = localDirsHandleService

  def setSpringEnv(springEnv: Environment): Unit =
    this.springEnv = springEnv

  override def handleInitEngineConnResources(
      request: EngineConnLaunchRequest,
      engineConn: EngineConn
  ): Unit = {
    // TODO: engineType判断是否下载到本地 unzip
    // engine_type resourceId version判断是否更新，或者重新下载，将path给到properties
    request match {
      case request: ProcessEngineConnLaunchRequest =>
        val files = request.bmlResources
        val linkDirsP = new mutable.HashMap[String, String]
        val user = request.user
        val ticketId = request.ticketId
        val engineType = LabelUtil.getEngineType(request.labels)
        val workDir = createDirIfNotExit(
          localDirsHandleService.getEngineConnWorkDir(user, ticketId, engineType)
        )
        val emHomeDir = createDirIfNotExit(localDirsHandleService.getEngineConnManagerHomeDir)
        val logDirs = createDirIfNotExit(
          localDirsHandleService.getEngineConnLogDir(user, ticketId, engineType)
        )
        val tmpDirs = createDirIfNotExit(
          localDirsHandleService.getEngineConnTmpDir(user, ticketId, engineType)
        )
        files.asScala.foreach(downloadBmlResource(request, linkDirsP, _, workDir))
        engineConn.getEngineConnLaunchRunner.getEngineConnLaunch.setEngineConnManagerEnv(
          new EngineConnManagerEnv {
            override val engineConnManagerHomeDir: String = emHomeDir
            override val engineConnWorkDir: String = workDir
            override val engineConnLogDirs: String = logDirs
            override val engineConnTempDirs: String = tmpDirs
            override val engineConnManagerHost: String = {
              var hostName = Utils.getComputerName
              val preferIpAddress = Configuration.PREFER_IP_ADDRESS
              logger.info("preferIpAddress:" + preferIpAddress)
              if (preferIpAddress) {
                hostName = springEnv.getProperty("spring.cloud.client.ip-address")
                logger.info("hostName:" + hostName)
                logger.info(
                  "using ip address replace hostname, because linkis.discovery.prefer-ip-address: true"
                )
              }
              hostName
            }
            override val engineConnManagerPort: String =
              DataWorkCloudApplication.getApplicationContext.getEnvironment.getProperty(
                "server.port"
              )
            override val linkDirs: mutable.HashMap[String, String] = linkDirsP
            override val properties: Map[String, String] = Map()
          }
        )
      case _ =>
    }
  }

  private def createDirIfNotExit(noSchemaPath: String): String = {
    val fsPath = new FsPath(schema + noSchemaPath)
    if (!fs.exists(fsPath)) {
      FileSystemUtils.mkdirs(fs, fsPath, Utils.getJvmUser)
      fs.setPermission(fsPath, "rwxrwxrwx")
    }
    noSchemaPath
  }

  def downloadBmlResource(
      request: ProcessEngineConnLaunchRequest,
      linkDirs: mutable.HashMap[String, String],
      resource: BmlResource,
      workDir: String
  ): Unit = {
    val resourceId = resource.getResourceId
    val version = resource.getVersion
    val user = request.user
    resource.getVisibility match {
      case BmlResource.BmlResourceVisibility.Public =>
        val publicDir = localDirsHandleService.getEngineConnPublicDir
        val bmlResourceDir = schema + Paths.get(publicDir, resourceId, version).toFile.getPath
        val fsPath = new FsPath(bmlResourceDir)

        /**
         * Prevent concurrent use of resourceId as a lock object
         */
        if (!fs.exists(fsPath)) resourceId.intern().synchronized {
          if (!fs.exists(fsPath)) {
            ECMUtils.downLoadBmlResourceToLocal(resource, user, fsPath.getPath)
            val unzipDir = fsPath.getSchemaPath + File.separator + resource.getFileName
              .substring(0, resource.getFileName.lastIndexOf("."))
            FileSystemUtils.mkdirs(fs, new FsPath(unzipDir), Utils.getJvmUser)
            val path = bmlResourceDir + File.separator + resource.getFileName
            Utils.tryCatch(ZipUtils.unzip(path, unzipDir)) { t: Throwable =>
              logger.error("Failed to unzip path", t)
              Utils.tryAndWarn(fs.delete(fsPath))
            }
            logger.info(s"Finished to download bml resource ${fsPath.getPath}")
            fs.delete(new FsPath(bmlResourceDir + File.separator + resource.getFileName))
          }
        }
        // 2.软连，并且添加到map
        val dirAndFileList = fs.listPathWithError(fsPath)

        var paths = dirAndFileList.getFsPaths.asScala

        if (paths.exists(_.getPath.endsWith(".zip"))) {
          logger.info(s"Start to wait fs path to init ${fsPath.getPath}")
          resourceId.intern().synchronized {
            logger.info(s"Finished to wait fs path to init ${fsPath.getPath} ")
          }
          paths = fs.listPathWithError(fsPath).getFsPaths.asScala
        }
        paths.foreach { path =>
          val name = new File(path.getPath).getName
          linkDirs.put(path.getPath, workDir + seperator + name)
        }
      case BmlResource.BmlResourceVisibility.Private =>
        logger.info(
          s"Try to download private BmlResource(resourceId: $resourceId, version: $version, fileName: ${resource.getFileName}) to path $workDir."
        )
        val fsPath = new FsPath(schema + workDir)
        if (!fs.exists(fsPath)) {
          FileSystemUtils.mkdirs(fs, fsPath, Utils.getJvmUser)
        }
        ECMUtils.downLoadBmlResourceToLocal(resource, user, fsPath.getPath)
        val filePath = schema + workDir + File.separator + resource.getFileName
        if (resource.getFileName != null && resource.getFileName.endsWith(".zip")) {
          logger.info(s"Try to unzip $filePath, since the private BMLResource is a zip file.")
          ZipUtils.unzip(filePath, fsPath.getSchemaPath)
          fs.delete(new FsPath(filePath))
        }
        logger.info(
          s"Finished to download private BmlResource(resourceId: $resourceId, version: $version, fileName: ${resource.getFileName}) to path $filePath."
        )
      case BmlResource.BmlResourceVisibility.Label =>
        logger.error(
          s"Not supported BmlResource visibility type: label. BmlResource: resourceId: $resourceId, version: $version, fileName: ${resource.getFileName}."
        )
        throw new ECMErrorException(
          NOT_SUPPORTED_TYPE.getErrorCode,
          NOT_SUPPORTED_TYPE.getErrorDesc
        )
    }
  }

}
