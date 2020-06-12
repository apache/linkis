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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import java.util.regex.Pattern

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.{PythonCodeCheckException, ScalaCodeCheckException}
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ArrayBuffer

/**
  * created by enjoyyin on 2018/10/19
  * Description:
  */
abstract class Explain extends Logging {
  /**
    * 用于检查code是否符合规范
    * @param code
    * @param error
    * @return
    */
  @throws[ErrorException]
  def authPass(code: String, error: StringBuilder): Boolean
}

object SparkExplain extends Explain {
  private val scStop = Pattern.compile("sc\\.stop(\\s*)")
  private val systemExit = Pattern.compile("System\\.exit")
  private val sy = Pattern.compile("sys\\.")
  private val scCancelAllJobs = Pattern.compile("sc\\.cancelAllJobs(\\s*)")
  private val runtime = Pattern.compile("Runtime\\.getRuntime")
  private val LOG:Logger = LoggerFactory.getLogger(getClass)
  override def authPass(code: String, error: StringBuilder): Boolean = {

    if (scStop.matcher(code).find()) {
      error.append("Authentication error: sc.stop() is not allowed in IDE.")
      false
    } else if (systemExit.matcher(code).find()) {
      error.append("Authentication error: System.exit(exitCode) is not allowed in IDE.")
      false
    } else if (sy.matcher(code).find()) {
      error.append("Authentication error: the package `sys` cannot be used in IDE.")
      false
    } else if (scCancelAllJobs.matcher(code).find()) {
      error.append("Authentication error: sc.cancelAllJobs() is not allowed in IDE.")
      false
    } else if (runtime.matcher(code).find()) {
      error.append("Authentication error: Runtime.getRuntime is not allowed in IDE.")
      false
    } else {
      true
    }
  }
}

object SQLExplain extends Explain {
  val SQL_DEFAULT_SEPERATOR = ";"
  val NO_LIMIT_STRING = "\n!with no limit!"

  val SQL_DEFAULT_LIMIT: CommonVars[Int] = CommonVars("wds.linkis.sql.default.limit", 5000)
  val SQL_APPEND_LIMIT:String = " limit " + SQL_DEFAULT_LIMIT.getValue
  val DROP_TABLE_SQL = "\\s*drop\\s+table\\s+\\w+\\s*"
  val CREATE_DATABASE_SQL = "\\s*create\\s+database\\s+\\w+\\s*"
  private val LINE_BREAK = "\n"
  private val COMMENT_FLAG = "--"
  val SET_OWN_USER = "set owner user"
  private val LIMIT:String = "limit"
  private val LIMIT_UPPERCASE:String = "LIMIT"
  private val IDE_ALLOW_NO_LIMIT = "--set wds.linkis.engine.no.limit.allow=true"
  private val LOG:Logger = LoggerFactory.getLogger(getClass)
  override def authPass(code: String, error: StringBuilder): Boolean = {
    if (code.trim.matches(CREATE_DATABASE_SQL)){
      error.append("Sorry, you have no permission to create database")
      false
    }else true
  }

  /**
    * to deal with sql limit
    * @param executionCode sql code
    * @param requestPersistTask use to store inited logs
    */
  def dealSQLLimit(executionCode:String, requestPersistTask: RequestPersistTask, logAppender: java.lang.StringBuilder):Unit = {
    val fixedCode:ArrayBuffer[String] = new ArrayBuffer[String]()
    executionCode.split(";") foreach { singleCode =>
      if (isSelectCmd(singleCode)){
        val trimCode = singleCode.trim
        if (isSelectCmdNoLimit(trimCode) && !executionCode.contains(IDE_ALLOW_NO_LIMIT)){
          logAppender.append(LogUtils.generateWarn(s"You submitted a sql $trimCode without limit, program will add limit 5000 to your sql") + "\n")
          fixedCode += (trimCode + SQL_APPEND_LIMIT)
        }else if (isSelectOverLimit(singleCode) && !executionCode.contains(IDE_ALLOW_NO_LIMIT)){
          val trimCode = singleCode.trim
          logAppender.append(LogUtils.generateWarn(s"You submitted a sql $trimCode with limit exceeding 5000, it is not allowed. Program will change your limit to 5000") + "\n")
          fixedCode += repairSelectOverLimit(trimCode)
        }else{
          fixedCode += singleCode.trim
        }
      }else{
        fixedCode += singleCode.trim
      }
    }
    logAppender.append(LogUtils.generateInfo("SQL code check has passed" + "\n"))
    requestPersistTask.setExecutionCode(fixedCode.mkString(";\n"))
  }


  private def addNoLimit(code: String) = code + NO_LIMIT_STRING

  protected def needNoLimit(code: String): Boolean = code.endsWith(NO_LIMIT_STRING)

  def isSelectCmd(code: String): Boolean = {
    if (StringUtils.isEmpty(code)) {
      return false
    }
    //如果一段sql是 --xxx回车select * from default.users，那么他也是select语句
    val realCode = cleanComment(code)
    realCode.trim.split("\\s+")(0).equalsIgnoreCase("select")
  }

  def continueWhenError = false

  def isSelectCmdNoLimit(cmd: String): Boolean = {
    if (StringUtils.isEmpty(cmd)) {
      return false
    }
    val realCode = cmd.trim
    //limit往往就是在sql语句中最后的，所以需要进行最后的判断
    val arr = realCode.split("\\s+")
    val words = new ArrayBuffer[String]()
    arr foreach {
      w => w.split("\n") foreach (words += _)
    }
    val a = words.toArray
    val length = a.length
    val second_last = a(length - 2)
    !"limit".equals(second_last.toLowerCase())
  }

  private def cleanComment(sql:String):String = {
    val cleanSql = new StringBuilder
    sql.trim.split(LINE_BREAK) foreach {
      singleSql => if (!singleSql.trim().startsWith(COMMENT_FLAG)) cleanSql.append(singleSql).append(LINE_BREAK)
    }
    cleanSql.toString().trim
  }

  def isSelectOverLimit(cmd: String): Boolean = {
    if (StringUtils.isEmpty(cmd)) {
      return false
    }
    var overLimit: Boolean = false
    var code = cmd.trim
    if (code.toLowerCase.contains("limit")) {
      code = code.substring(code.toLowerCase().lastIndexOf("limit")).trim
    }
    val hasLimit = code.toLowerCase().matches("limit\\s+\\d+\\s*;?")
    if (hasLimit) {
      if (code.indexOf(";") > 0) {
        code = code.substring(5, code.length - 1).trim
      } else {
        code = code.substring(5).trim
      }
      val limitNum = code.toInt
      if (limitNum > SQL_DEFAULT_LIMIT.getValue) {
        overLimit = true
      }
    }
    overLimit
  }

  /**
    * 修改正确
    * @param cmd code
    * @return String
    */
  def repairSelectOverLimit(cmd: String): String = {
    var code = cmd.trim
    var preCode=""
    var tailCode=""
    var limitNum = SQL_DEFAULT_LIMIT.getValue
    if (code.toLowerCase.contains("limit")) {
      preCode=code.substring(0,code.toLowerCase().lastIndexOf("limit")).trim
      tailCode = code.substring(code.toLowerCase().lastIndexOf("limit")).trim
    }
    if (isUpperSelect(cmd)) preCode + " LIMIT " + limitNum else preCode + " limit " + limitNum
  }

  private def isUpperSelect(selectSql:String):Boolean = {
    if (selectSql.trim.startsWith("SELECT")) true else false
  }


}

object PythonExplain extends Explain{
  /**
    * User is not allowed to import sys module(不允许用户导入sys模块)
    */
  private val IMPORT_SYS_MOUDLE = """import\s+sys""".r.unanchored
  private val FROM_SYS_IMPORT = """from\s+sys\s+import\s+.*""".r.unanchored
  /**
    * User is not allowed to import os module(不允许用户导入os模块)
    */
  private val IMPORT_OS_MOUDLE = """import\s+os""".r.unanchored
  private val FROM_OS_IMPORT = """from\s+os\s+import\s+.*""".r.unanchored
  /**
    * Do not allow users to open the process privately(不允许用户私自开启进程)
    */
  private val IMPORT_PROCESS_MODULE = """import\s+multiprocessing""".r.unanchored
  private val FROM_MULTIPROCESS_IMPORT = """from\s+multiprocessing\s+import\s+.*""".r.unanchored
  private val IMPORT_SUBPORCESS_MODULE = """import\s+subprocess""".r.unanchored
  private val FROM_SUBPROCESS_IMPORT = """from\s+subprocess\s+import\s+.*""".r.unanchored
  /**
    * Forbidden user stop sparkContext(禁止用户stop sparkContext)
    */
  private val SC_STOP = """sc\.stop""".r.unanchored
  override def authPass(code: String, error: StringBuilder): Boolean = {
    code.split(System.lineSeparator()) foreach {code =>
      if (IMPORT_SYS_MOUDLE.findAllIn(code).nonEmpty || FROM_SYS_IMPORT.findAllIn(code).nonEmpty)
        throw PythonCodeCheckException(20070, "can not use sys module")
      else if (IMPORT_OS_MOUDLE.findAllIn(code).nonEmpty || FROM_OS_IMPORT.findAllIn(code).nonEmpty)
        throw PythonCodeCheckException(20071, "can not use os moudle")
      else if (IMPORT_PROCESS_MODULE.findAllIn(code).nonEmpty || FROM_MULTIPROCESS_IMPORT.findAllIn(code).nonEmpty)
        throw PythonCodeCheckException(20072, "can not use process module")
      else if (IMPORT_SUBPORCESS_MODULE.findAllIn(code).nonEmpty || FROM_SUBPROCESS_IMPORT.findAllIn(code).nonEmpty)
        throw PythonCodeCheckException(20072, "can not use subprocess module")
      else if (SC_STOP.findAllIn(code).nonEmpty)
        throw PythonCodeCheckException(20073, "You can not stop SparkContext, It's dangerous")
    }
    true
  }
}

object ScalaExplain extends Explain{
  private val systemExit = """System.exit""".r.unanchored
  private val runtime = """Runtime.getRunTime""".r.unanchored
  private val LOG:Logger = LoggerFactory.getLogger(getClass)
  override def authPass(code: String, error: StringBuilder): Boolean = {
    code match {
      case systemExit() => LOG.error("scala code can not use System.exit")
        throw ScalaCodeCheckException(20074, "scala code can not use System.exit")
      case runtime() => LOG.error("scala code can not use Runtime.getRuntime")
        throw ScalaCodeCheckException(20074, "scala code can not use Runtime.getRuntime")
      case _ => true
    }
  }
}