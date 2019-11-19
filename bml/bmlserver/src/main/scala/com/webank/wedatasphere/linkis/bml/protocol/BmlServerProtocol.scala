package com.webank.wedatasphere.linkis.bml.protocol

/**
  * created by cooperyang on 2019/5/28
  * Description:
  */
trait BmlServerProtocol

case class BmlDownloadElementProtocol(path:String,
                                   startByte:Long,
                                   endByte:Long) extends BmlServerProtocol

