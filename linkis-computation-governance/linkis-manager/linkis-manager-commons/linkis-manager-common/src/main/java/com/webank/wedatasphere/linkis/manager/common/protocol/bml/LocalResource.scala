package com.webank.wedatasphere.linkis.manager.common.protocol.bml

import java.util.Date

/**
 * created by cooperyang on 2020/7/10
 * Description:
 */
trait LocalResource extends BmlResource {

  def setIsPublicPath(isPublicPath:Boolean):Unit

  def getIsPublicPath:Boolean

  def setPath(path:String):Unit

  def getPath:String

  def setDownloadTime(date:Date):Unit

  def getDownloadTime:Date

}
