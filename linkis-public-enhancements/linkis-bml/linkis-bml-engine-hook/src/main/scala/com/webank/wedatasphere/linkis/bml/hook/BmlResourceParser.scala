package com.webank.wedatasphere.linkis.bml.hook

case class ResourceVersion(resourceId:String, version:String)


trait BmlResourceParser {
  /**
    * 通过传入的code
    * @param code
    * @return
    */
  def getResource(code:String):Array[ResourceVersion]
}


object DefaultBmlResourceParser extends BmlResourceParser{
  /**
    * 通过传入的code
    *
    * @param code
    * @return
    */
  override def getResource(code: String): Array[ResourceVersion] = Array.empty
}