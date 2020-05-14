package com.webank.wedatasphere.linkis.entrance.utils

import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer

/**
 * Created by yogafire on 2020/5/11
 */
//FIXME 能不能把parser统一到common包里？
object SqlCodeParser {
  val separator = ";"
  val defaultLimit: Int = PrestoConfiguration.PRESTO_DEFAULT_LIMIT.getValue

  def parse(code: String): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]()

    def appendStatement(sqlStatement: String): Unit = {
      codeBuffer.append(sqlStatement)
    }

    if (StringUtils.contains(code, separator)) {
      StringUtils.split(code, ";").foreach {
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s + " limit " + defaultLimit);
        case s => appendStatement(s);
      }
    } else {
      code match {
        case s if StringUtils.isBlank(s) =>
        case s if isSelectCmdNoLimit(s) => appendStatement(s + " limit " + defaultLimit);
        case s => appendStatement(s);
      }
    }
    codeBuffer.toArray
  }

  def isSelectCmdNoLimit(cmd: String): Boolean = {
    var code = cmd.trim
    if (!cmd.split("\\s+")(0).equalsIgnoreCase("select")) return false
    if (code.contains("limit")) code = code.substring(code.lastIndexOf("limit")).trim
    else if (code.contains("LIMIT")) code = code.substring(code.lastIndexOf("LIMIT")).trim.toLowerCase
    else return true
    val hasLimit = code.matches("limit\\s+\\d+\\s*;?")
    if (hasLimit) {
      if (code.indexOf(";") > 0) code = code.substring(5, code.length - 1).trim
      else code = code.substring(5).trim
      val limitNum = code.toInt
      if (limitNum > defaultLimit) throw new IllegalArgumentException("We at most allowed to limit " + defaultLimit + ", but your SQL has been over the max rows.")
    }
    !hasLimit
  }
}
