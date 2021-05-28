package com.webank.wedatasphere.linkis.errorcode.common

import scala.util.matching.Regex


trait ErrorCode {

  def getErrorCode:String

  def getErrorDesc:String

  def getErrorRegex:Regex

  def getErrorRegexStr:String
}
