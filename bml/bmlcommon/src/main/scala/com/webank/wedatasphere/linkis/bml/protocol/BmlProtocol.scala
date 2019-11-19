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
