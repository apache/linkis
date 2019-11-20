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
package com.webank.wedatasphere.linkis.bml.protocol

import java.io.InputStream

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
trait BmlProtocol {

}

trait BmlRequest extends BmlProtocol

abstract class BmlResponse(isSuccess:Boolean) extends BmlProtocol


//case class BmlUploadRequest() extends BmlRequest
//
//case class BmlDownloadRequest(resourceId:String,
//                              version:String
//                              ) extends BmlRequest
//
//case class BmlUpdateRequest(resourceID:String) extends BmlRequest


case class BmlUploadResponse(isSuccess:Boolean,
                             resourceId:String,
                             version:String) extends BmlResponse(isSuccess)

case class BmlDownloadResponse(isSuccess:Boolean,
                               inputStream:InputStream,
                               resourceId:String,
                               version:String,
                               fullFilePath:String) extends BmlResponse(isSuccess)



case class BmlUpdateResponse(isSuccess:Boolean,
                             resourceId:String,
                             version:String
                            ) extends BmlResponse(isSuccess)


case class BmlRelateResponse(isSuccess:Boolean,
                             resourceId:String,
                             version:String
                            ) extends BmlResponse(isSuccess)


case class Version(version:String,
                   resource:String)
case class ResourceVersions(resourceId:String,
                            user:String,
                            system:String,
                            versions:java.util.List[Version])


case class BmlResourceVersionsResponse(isSuccess:Boolean,
                                       resourceId:String,
                                       resourceVersions: ResourceVersions) extends BmlResponse(isSuccess)
