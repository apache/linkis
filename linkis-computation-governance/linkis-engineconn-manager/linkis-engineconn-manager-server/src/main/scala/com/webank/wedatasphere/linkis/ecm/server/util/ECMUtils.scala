/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.ecm.server.util

import java.io.{File, InputStream}
import java.util

import com.webank.wedatasphere.linkis.bml.client.{BmlClient, BmlClientFactory}
import com.webank.wedatasphere.linkis.bml.protocol.BmlDownloadResponse
import com.webank.wedatasphere.linkis.ecm.server.exception.ECMErrorException
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import org.apache.commons.io.{FileUtils, IOUtils}

import scala.collection.JavaConversions._


object ECMUtils {

  private def download(resource: BmlResource, userName: String): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    var response: BmlDownloadResponse = null
    if (resource.getVersion == null) {
      response = client.downloadShareResource(userName, resource.getResourceId)
    } else {
      response = client.downloadShareResource(userName, resource.getResourceId, resource.getVersion)
    }
    if (!response.isSuccess) throw new ECMErrorException(911115, "failed to downLoad(下载失败)")
    val map = new util.HashMap[String, Object]
    map += "path" -> response.fullFilePath
    map += "is" -> response.inputStream
  }

  def downLoadBmlResourceToLocal(resource: BmlResource, userName: String, path: String)(implicit fs: FileSystem): Unit = {
    val is = download(resource, userName).get("is").asInstanceOf[InputStream]
    val os = FileUtils.openOutputStream(new File(path + File.separator + resource.getFileName))
    IOUtils.copy(is, os)
    IOUtils.closeQuietly(os)
    IOUtils.closeQuietly(is)
  }

  private def createBMLClient(userName: String): BmlClient = {
    if (userName == null)
      BmlClientFactory.createBmlClient()
    else
      BmlClientFactory.createBmlClient(userName)
  }


  private val address = Sender.getThisInstance.substring(0, Sender.getThisInstance.lastIndexOf(":"))

  def getInstanceByPort(port: String): String = address + ":" + port

}
