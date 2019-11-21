/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.linkis.filesystem.bml

import java.io.{ByteArrayInputStream, InputStream}
import java.util
import java.util.UUID

import com.webank.wedatasphere.linkis.bml.client.{BmlClient, BmlClientFactory}
import com.webank.wedatasphere.linkis.bml.protocol.{BmlDownloadResponse, BmlUpdateResponse, BmlUploadResponse}
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
/**
  * Created by patinousward
  */
@Component
class BMLHelper {

  def upload(userName: String, content: String, fileName: String): util.Map[String, Object] = {
    val inputStream = new ByteArrayInputStream(content.getBytes("utf-8"))
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUploadResponse = client.uploadResource(userName, fileName, inputStream)
    if (!resource.isSuccess) throw new WorkSpaceException("上传失败")
    val map = new util.HashMap[String, Object]
    map += "resourceId" -> resource.resourceId
    map += "version" -> resource.version
  }

  def upload(userName: String, inputStream: InputStream, fileName: String): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUploadResponse = client.uploadResource(userName, fileName, inputStream)
    if (!resource.isSuccess) throw new WorkSpaceException("上传失败")
    val map = new util.HashMap[String, Object]
    map += "resourceId" -> resource.resourceId
    map += "version" -> resource.version
  }

  def update(userName: String, resourceId: String, inputStream: InputStream): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUpdateResponse = client.updateResource(userName, resourceId, "", inputStream)
    if (!resource.isSuccess) throw new WorkSpaceException("更新失败")
    val map = new util.HashMap[String, Object]
    map += "resourceId" -> resource.resourceId
    map += "version" -> resource.version
  }

  def update(userName: String, resourceId: String, content: String): util.Map[String, Object] = {
    val inputStream = new ByteArrayInputStream(content.getBytes("utf-8"))
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUpdateResponse = client.updateResource(userName, resourceId, UUID.randomUUID().toString+".json", inputStream)
    if (!resource.isSuccess) throw new WorkSpaceException("更新失败")
    val map = new util.HashMap[String, Object]
    map += "resourceId" -> resource.resourceId
    map += "version" -> resource.version
  }

  def query(userName: String, resourceId: String, version: String): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    var resource: BmlDownloadResponse = null
    if (version == null) resource = client.downloadResource(userName, resourceId,null) else resource = client.downloadResource(userName, resourceId, version)
    if (!resource.isSuccess) throw new WorkSpaceException("下载失败")
    val map = new util.HashMap[String, Object]
    map += "path" -> resource.fullFilePath
    map += "stream" ->resource.inputStream
  }

  private def inputstremToString(inputStream: InputStream): String = scala.io.Source.fromInputStream(inputStream).mkString

  private def createBMLClient(userName: String): BmlClient = if (userName == null)
    BmlClientFactory.createBmlClient()
  else
    BmlClientFactory.createBmlClient(userName)
}
