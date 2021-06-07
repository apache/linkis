package com.webank.wedatasphere.linkis.manager.common.protocol.bml

import java.util.Date


trait LocalResource extends BmlResource {

  def setIsPublicPath(isPublicPath:Boolean):Unit

  def getIsPublicPath:Boolean

  def setPath(path:String):Unit

  def getPath:String

  def setDownloadTime(date:Date):Unit

  def getDownloadTime:Date

}
