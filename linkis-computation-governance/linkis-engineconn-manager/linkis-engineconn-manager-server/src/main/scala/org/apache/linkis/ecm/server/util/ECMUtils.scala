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

package org.apache.linkis.ecm.server.util

import org.apache.linkis.bml.client.{BmlClient, BmlClientFactory}
import org.apache.linkis.bml.protocol.BmlDownloadResponse
import org.apache.linkis.common.utils.{HardwareUtils, Logging}
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary.FAILED_TO_DOWNLOAD
import org.apache.linkis.ecm.server.conf.ECMConfiguration.{
  ECM_MAX_CORES_AVAILABLE,
  ECM_MAX_CREATE_INSTANCES,
  ECM_MAX_MEMORY_AVAILABLE,
  ECM_PROTECTED_CORES,
  ECM_PROTECTED_INSTANCES,
  ECM_PROTECTED_MEMORY,
  ECM_STIMATE_ACTUAL_MEMORY_ENABLE
}
import org.apache.linkis.ecm.server.exception.ECMErrorException
import org.apache.linkis.manager.common.entity.resource.LoadInstanceResource
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.rpc.Sender
import org.apache.linkis.storage.fs.FileSystem

import org.apache.commons.io.{FileUtils, IOUtils}

import java.io.{File, InputStream}
import java.util

import scala.collection.JavaConverters._

object ECMUtils extends Logging {

  @volatile var bmlClient: BmlClient = _
  val lock = new Object()

  private def download(resource: BmlResource, userName: String): util.Map[String, Object] = {
    val client: BmlClient = getBMLClient(userName)
    var response: BmlDownloadResponse = null
    if (resource.getVersion == null) {
      response = client.downloadShareResource(userName, resource.getResourceId)
    } else {
      response = client.downloadShareResource(userName, resource.getResourceId, resource.getVersion)
    }
    if (!response.isSuccess) {
      throw new ECMErrorException(FAILED_TO_DOWNLOAD.getErrorCode, FAILED_TO_DOWNLOAD.getErrorDesc)
    }
    val map = new util.HashMap[String, Object]
    map.put("path", response.fullFilePath)
    map.put("is", response.inputStream)
    map
  }

  def downLoadBmlResourceToLocal(resource: BmlResource, userName: String, path: String)(implicit
      fs: FileSystem
  ): Unit = {
    val is = download(resource, userName).get("is").asInstanceOf[InputStream]
    val os = FileUtils.openOutputStream(new File(path + File.separator + resource.getFileName))
    IOUtils.copy(is, os)
    IOUtils.closeQuietly(os)
    IOUtils.closeQuietly(is)
  }

  private def getBMLClient(userName: String): BmlClient = {
    if (bmlClient == null) {
      lock.synchronized {
        if (bmlClient == null) bmlClient = BmlClientFactory.createBmlClient(userName)
      }
    }
    bmlClient
  }

  private val address =
    Sender.getThisInstance.substring(0, Sender.getThisInstance.lastIndexOf(":"))

  val initMaxResource: LoadInstanceResource = {
    new LoadInstanceResource(inferDefaultMemory, ECM_MAX_CORES_AVAILABLE, ECM_MAX_CREATE_INSTANCES)
  }

  val initMinResource: LoadInstanceResource = {
    new LoadInstanceResource(ECM_PROTECTED_MEMORY, ECM_PROTECTED_CORES, ECM_PROTECTED_INSTANCES)
  }

  def getInstanceByPort(port: String): String = address + ":" + port

  // ecm machine memory
  def inferDefaultMemory(): Long = {
    // if enable estimate actual memory
    if (ECM_STIMATE_ACTUAL_MEMORY_ENABLE) {

      val totalByte = HardwareUtils.getMaxMemory()

      val resultMemory = math.max(totalByte, ECM_PROTECTED_MEMORY)
      // max of PhysicalMemory or ECM_PROTECTED_MEMORY
      logger.info(
        s"Ecm protected memory:${ECM_PROTECTED_MEMORY} byte, ecm machine physical max memory:${totalByte} byte, will use the lager one:${resultMemory}"
      )
      resultMemory

    } else {
      ECM_MAX_MEMORY_AVAILABLE
    }
  }

}
