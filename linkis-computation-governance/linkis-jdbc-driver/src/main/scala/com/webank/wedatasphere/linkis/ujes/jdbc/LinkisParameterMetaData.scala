package com.webank.wedatasphere.linkis.ujes.jdbc

import java.sql.ParameterMetaData

/**
  * @author peacewong
  * @date 2020/5/30 23:22
  */
class LinkisParameterMetaData(parameterCount: Int)  extends ParameterMetaData{

  override def getParameterCount: Int = parameterCount

  override def isNullable(param: Int): Int = 1

  override def isSigned(param: Int): Boolean = true

  override def getPrecision(param: Int): Int = Int.MaxValue

  override def getScale(param: Int): Int = 10

  override def getParameterType(param: Int): Int = 12

  override def getParameterTypeName(param: Int): String = "VARCHAR"

  override def getParameterClassName(param: Int): String = "java.lang.String"

  override def getParameterMode(param: Int): Int = 1

  override def unwrap[T](iface: Class[T]): T = iface.cast(this)

  override def isWrapperFor(iface: Class[_]): Boolean = iface.isInstance(this)
}
