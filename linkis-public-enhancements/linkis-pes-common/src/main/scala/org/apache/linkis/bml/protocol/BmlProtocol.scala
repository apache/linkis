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

package org.apache.linkis.bml.protocol

import java.io.InputStream
import java.util

trait BmlProtocol {}

trait BmlRequest extends BmlProtocol

abstract class BmlResponse(isSuccess: Boolean) extends BmlProtocol

// case class BmlUploadRequest() extends BmlRequest
//
// case class BmlDownloadRequest(resourceId:String,
//                              version:String
//                              ) extends BmlRequest
//
// case class BmlUpdateRequest(resourceID:String) extends BmlRequest

case class BmlUploadResponse(isSuccess: Boolean, resourceId: String, version: String)
    extends BmlResponse(isSuccess)

case class BmlDownloadResponse(
    isSuccess: Boolean,
    inputStream: InputStream,
    resourceId: String,
    version: String,
    fullFilePath: String
) extends BmlResponse(isSuccess)

case class BmlUpdateResponse(isSuccess: Boolean, resourceId: String, version: String)
    extends BmlResponse(isSuccess)

case class BmlRelateResponse(isSuccess: Boolean, resourceId: String, version: String)
    extends BmlResponse(isSuccess)

case class Version(version: String, resource: String)

case class ResourceVersions(
    resourceId: String,
    user: String,
    system: String,
    versions: java.util.List[Version]
)

case class BmlResourceVersionsResponse(
    isSuccess: Boolean,
    resourceId: String,
    resourceVersions: ResourceVersions
) extends BmlResponse(isSuccess)

case class BmlDeleteResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlCreateProjectResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlProjectInfoResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlResourceInfoResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlProjectPrivResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlAttachResourceAndProjectResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlUpdateProjectPrivResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlChangeOwnerResponse(isSuccess: Boolean) extends BmlResponse(isSuccess)

case class BmlCopyResourceResponse(isSuccess: Boolean, resourceId: String)
    extends BmlResponse(isSuccess)

case class BmlRollbackVersionResponse(isSuccess: Boolean, resourceId: String, version: String)
    extends BmlResponse(isSuccess)

case class BmlClientConnectInfoResponse(
    isSuccess: Boolean,
    clientConnectInfo: util.HashMap[String, Int]
) extends BmlResponse(isSuccess)
