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

package org.apache.linkis.bml.response

import org.apache.linkis.bml.protocol.ResourceVersions
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.io.InputStream

abstract class BmlResult extends DWSResult {
  private var resourceId: String = _
  private var version: String = _

  def setResourceId(resourceId: String): Unit = this.resourceId = resourceId
  def getResourceId: String = this.resourceId

  def setVersion(version: String): Unit = this.version = version
  def getVersion: String = this.version

  def getResultType: String = this.getClass.toString

}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/updateVersion")
class BmlUpdateResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/updateShareResource")
class BmlUpdateShareResourceResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/upload")
class BmlUploadResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/uploadShareResource")
class BmlUploadShareResourceResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/updateBasic")
class BmlUpdateBasicResult extends BmlResult {}

class BmlRelateResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/relateStorage")
class BmlRelateStorageResult extends BmlRelateResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/relateHdfs")
class BmlRelateHdfsResult extends BmlRelateResult {}

// case class Version(version:String,
//                   resource:String)
// case class ResourceVersions(resourceId:String,
//                           user:String,
//                           system:String,
//                           versions:java.util.List[Version])

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/getVersions")
class BmlResourceVersionResult extends BmlResult {
  var resourceVersions: ResourceVersions = _

  def setResourceVersions(resourceVersions: ResourceVersions): Unit = this.resourceVersions =
    resourceVersions

  def getResourceVersions: ResourceVersions = this.resourceVersions
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/download")
class BmlResourceDownloadResult extends BmlResult {
  var inputStream: InputStream = _
  def setInputStream(inputStream: InputStream): Unit = this.inputStream = inputStream
  def getInputStream: InputStream = this.inputStream
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/getBasic")
class BmlGetBasicResult extends BmlResult {
  var downloadedFileName: String = _
  def getDownloadedFileName: String = this.downloadedFileName

  def setDownloadedFileName(downloadedFileName: String): Unit = this.downloadedFileName =
    downloadedFileName

}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/delete")
class BmlDeleteResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/createBmlProject")
class BmlCreateBmlProjectResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/attachResourceAndProject")
class BmlAttachResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/downloadShareResource")
class BmlDownloadShareResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/updateProjectUsers")
class BmlUpdateProjectResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/rollbackVersion")
class BmlRollbackVersionResult extends BmlResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/bml/copyResourceToAnotherUser")
class BmlCopyResourceResult extends BmlResult {}
