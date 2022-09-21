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

package org.apache.linkis.filesystem.bml

import org.apache.linkis.bml.client.{BmlClient, BmlClientFactory}
import org.apache.linkis.bml.protocol.{BmlDownloadResponse, BmlUpdateResponse, BmlUploadResponse}
import org.apache.linkis.filesystem.exception.WorkspaceExceptionManager

import org.springframework.stereotype.Component

import java.io.{ByteArrayInputStream, InputStream}
import java.util
import java.util.UUID

@Component
class BMLHelper {

  def upload(userName: String, content: String, fileName: String): util.Map[String, Object] = {
    val inputStream = new ByteArrayInputStream(content.getBytes("utf-8"))
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUploadResponse = client.uploadResource(userName, fileName, inputStream)
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80021)
    val map = new util.HashMap[String, Object]
    map.put("resourceId", resource.resourceId)
    map.put("version", resource.version)
    map
  }

  def upload(
      userName: String,
      inputStream: InputStream,
      fileName: String,
      projectName: String
  ): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUploadResponse =
      client.uploadShareResource(userName, projectName, fileName, inputStream)
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80021)
    val map = new util.HashMap[String, Object]
    map.put("resourceId", resource.resourceId)
    map.put("version", resource.version)
    map
  }

  def upload(
      userName: String,
      inputStream: InputStream,
      fileName: String
  ): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUploadResponse = client.uploadResource(userName, fileName, inputStream)
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80021)
    val map = new util.HashMap[String, Object]
    map.put("resourceId", resource.resourceId)
    map.put("version", resource.version)
    map
  }

  def update(
      userName: String,
      resourceId: String,
      inputStream: InputStream
  ): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUpdateResponse =
      client.updateShareResource(userName, resourceId, "", inputStream)
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80022)
    val map = new util.HashMap[String, Object]
    map.put("resourceId", resource.resourceId)
    map.put("version", resource.version)
    map
  }

  def update(userName: String, resourceId: String, content: String): util.Map[String, Object] = {
    val inputStream = new ByteArrayInputStream(content.getBytes("utf-8"))
    val client: BmlClient = createBMLClient(userName)
    val resource: BmlUpdateResponse = client.updateShareResource(
      userName,
      resourceId,
      UUID.randomUUID().toString + ".json",
      inputStream
    )
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80022)
    val map = new util.HashMap[String, Object]
    map.put("resourceId", resource.resourceId)
    map.put("version", resource.version)
    map
  }

  def query(userName: String, resourceId: String, version: String): util.Map[String, Object] = {
    val client: BmlClient = createBMLClient(userName)
    var resource: BmlDownloadResponse = null
    if (version == null) resource = client.downloadShareResource(userName, resourceId, null)
    else resource = client.downloadShareResource(userName, resourceId, version)
    if (!resource.isSuccess) throw WorkspaceExceptionManager.createException(80023)
    val map = new util.HashMap[String, Object]
    map.put("path", resource.fullFilePath)
    map.put("stream", resource.inputStream)
    map
  }

  private def inputstremToString(inputStream: InputStream): String =
    scala.io.Source.fromInputStream(inputStream).mkString

  private def createBMLClient(userName: String): BmlClient = if (userName == null) {
    BmlClientFactory.createBmlClient()
  } else {
    BmlClientFactory.createBmlClient(userName)
  }

}
