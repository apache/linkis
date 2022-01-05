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
 
package org.apache.linkis.engineplugin.server.service

import java.util.Date

import org.apache.linkis.bml.client.BmlClientFactory
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration
import org.apache.linkis.engineplugin.server.dao.EngineConnBmlResourceDao
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource
import org.apache.linkis.engineplugin.server.localize.{EngineConnBmlResourceGenerator, EngineConnLocalizeResource}
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.common.protocol.bml.BmlResource.BmlResourceVisibility
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import org.apache.linkis.manager.engineplugin.common.launch.process.{EngineConnResource, LaunchConstants}
import org.apache.linkis.message.annotation.Receiver
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class DefaultEngineConnResourceService extends EngineConnResourceService with Logging {

  @Autowired
  private var engineConnBmlResourceGenerator: EngineConnBmlResourceGenerator = _
  @Autowired
  private var engineConnBmlResourceDao: EngineConnBmlResourceDao = _
  private val bmlClient = BmlClientFactory.createBmlClient()
  private var isRefreshing: Boolean = false

  @PostConstruct
  override def init(): Unit = if (EngineConnPluginConfiguration.ENGINE_CONN_DIST_LOAD_ENABLE.getValue) {
    info("Start to refresh all engineconn plugins when inited.")
    refreshAll(false)
  }


  private def uploadToBml(localizeResource: EngineConnLocalizeResource): BmlResource = {
    val response = bmlClient.uploadResource(Utils.getJvmUser, localizeResource.fileName, localizeResource.getFileInputStream)
    val bmlResource = new BmlResource
    bmlResource.setResourceId(response.resourceId)
    bmlResource.setVersion(response.version)
    bmlResource
  }

  private def uploadToBml(localizeResource: EngineConnLocalizeResource, resourceId: String): BmlResource = {
    val response = bmlClient.updateResource(Utils.getJvmUser, resourceId, localizeResource.fileName, localizeResource.getFileInputStream)
    val bmlResource = new BmlResource
    bmlResource.setResourceId(response.resourceId)
    bmlResource.setVersion(response.version)
    bmlResource
  }

  override def refreshAll(wait: Boolean = false): Unit = {
    if (!isRefreshing) {
      synchronized {
        if (!isRefreshing) {
          val refreshTask = new Runnable {
            override def run(): Unit = {
              isRefreshing = true
              info(s"Try to initialize the dist resources of all EngineConns.")
              engineConnBmlResourceGenerator.getEngineConnTypeListFromDisk foreach { engineConnType =>
                Utils.tryAndError {
                  info(s"Try to initialize all versions of ${engineConnType}EngineConn.")
                  engineConnBmlResourceGenerator.generate(engineConnType).foreach { case (version, localize) =>
                    info(s"Try to initialize ${engineConnType}EngineConn-$version.")
                    refresh(localize, engineConnType, version)
                  }
                }
              }
              isRefreshing = false
            }
          }
          val future = Utils.defaultScheduler.submit(refreshTask)
          if (wait) {
            Utils.tryAndWarn(future.get())
          }
        } else {
          info ("IsRefreshing EngineConns...")
        }
      }
    }
  }

  @Receiver
  def refeshAll(engineConnRefreshAllRequest: RefreshAllEngineConnResourceRequest): Boolean = {
    info("Start to refresh all engineconn plugins.")
    refreshAll(true)
    true
  }

  @Receiver
  override def refresh(engineConnRefreshRequest: RefreshEngineConnResourceRequest): Boolean = {
    val engineConnType = engineConnRefreshRequest.getEngineConnType
    val version = engineConnRefreshRequest.getVersion
    if ("*" == version || StringUtils.isEmpty(version)) {
      info(s"Try to refresh all versions of ${engineConnType}EngineConn.")
      engineConnBmlResourceGenerator.generate(engineConnType).foreach { case (v, localize) =>
        info(s"Try to refresh ${engineConnType}EngineConn-$v.")
        refresh(localize, engineConnType, v)
      }
    } else {
      info(s"Try to refresh ${engineConnType}EngineConn-$version.")
      val localize = engineConnBmlResourceGenerator.generate(engineConnType, version)
      refresh(localize, engineConnType, version)
    }
    true
  }

  private def refresh(localize: Array[EngineConnLocalizeResource], engineConnType: String, version: String): Unit = {
    val engineConnBmlResources = asScalaBuffer(engineConnBmlResourceDao.getAllEngineConnBmlResource(engineConnType, version))
    if(localize.count(localizeResource => localizeResource.fileName == LaunchConstants.ENGINE_CONN_CONF_DIR_NAME + ".zip" ||
      localizeResource.fileName == LaunchConstants.ENGINE_CONN_LIB_DIR_NAME + ".zip") < 2)
      throw new EngineConnPluginErrorException(20001, s"The `lib` and `conf` dir is necessary in ${engineConnType}EngineConn dist.")
    localize.foreach { localizeResource =>
      val resource = engineConnBmlResources.find(_.getFileName == localizeResource.fileName)
      if(resource.isEmpty) {
        info(s"Ready to upload a new bmlResource for ${engineConnType}EngineConn-$version. path: " + localizeResource.fileName)
        val bmlResource = uploadToBml(localizeResource)
        val engineConnBmlResource = new EngineConnBmlResource
        engineConnBmlResource.setBmlResourceId(bmlResource.getResourceId)
        engineConnBmlResource.setBmlResourceVersion(bmlResource.getVersion)
        engineConnBmlResource.setCreateTime(new Date)
        engineConnBmlResource.setLastUpdateTime(new Date)
        engineConnBmlResource.setEngineConnType(engineConnType)
        engineConnBmlResource.setFileName(localizeResource.fileName)
        engineConnBmlResource.setFileSize(localizeResource.fileSize)
        engineConnBmlResource.setLastModified(localizeResource.lastModified)
        engineConnBmlResource.setVersion(version)
        engineConnBmlResourceDao.save(engineConnBmlResource)
      } else if(resource.exists(r => r.getFileSize != localizeResource.fileSize || r.getLastModified != localizeResource.lastModified)) {
        info(s"Ready to upload a refreshed bmlResource for ${engineConnType}EngineConn-$version. path: " + localizeResource.fileName)
        val engineConnBmlResource = resource.get
        val bmlResource = uploadToBml(localizeResource, engineConnBmlResource.getBmlResourceId)
        engineConnBmlResource.setBmlResourceVersion(bmlResource.getVersion)
        engineConnBmlResource.setLastUpdateTime(new Date)
        engineConnBmlResource.setFileSize(localizeResource.fileSize)
        engineConnBmlResource.setLastModified(localizeResource.lastModified)
        engineConnBmlResourceDao.update(engineConnBmlResource)
      } else info(s"The file has no change in ${engineConnType}EngineConn-$version, path: " + localizeResource.fileName)
    }
  }

  @Receiver
  override def getEngineConnBMLResources(engineConnBMLResourceRequest: GetEngineConnResourceRequest): EngineConnResource = {
    val engineConnType = engineConnBMLResourceRequest.getEngineConnType
    val version = engineConnBMLResourceRequest.getVersion
    val engineConnBmlResources = asScalaBuffer(engineConnBmlResourceDao.getAllEngineConnBmlResource(engineConnType, "v" + version))
    val confBmlResource = engineConnBmlResources.find(_.getFileName == LaunchConstants.ENGINE_CONN_CONF_DIR_NAME + ".zip").map(parseToBmlResource).get
    val libBmlResource = engineConnBmlResources.find(_.getFileName == LaunchConstants.ENGINE_CONN_LIB_DIR_NAME + ".zip").map(parseToBmlResource).get
    val otherBmlResources = engineConnBmlResources.filterNot(r => r.getFileName == LaunchConstants.ENGINE_CONN_CONF_DIR_NAME + ".zip" ||
      r.getFileName == LaunchConstants.ENGINE_CONN_LIB_DIR_NAME + ".zip").map(parseToBmlResource).toArray
    new EngineConnResource {
      override def getConfBmlResource: BmlResource = confBmlResource

      override def getLibBmlResource: BmlResource = libBmlResource

      override def getOtherBmlResources: Array[BmlResource] = otherBmlResources
    }
  }

  private def parseToBmlResource(engineConnBmlResource: EngineConnBmlResource): BmlResource = {
    val bmlResource = new BmlResource
    bmlResource.setFileName(engineConnBmlResource.getFileName)
    bmlResource.setOwner(Utils.getJvmUser)
    bmlResource.setResourceId(engineConnBmlResource.getBmlResourceId)
    bmlResource.setVersion(engineConnBmlResource.getBmlResourceVersion)
    bmlResource.setVisibility(BmlResourceVisibility.Public)
    bmlResource
  }

}
