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

import java.lang
import java.util.regex.Pattern

import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import org.slf4j.{Logger, LoggerFactory}

import scala.util.matching.Regex

/**
  * created by enjoyyin on 2019/2/28
  * Description:
  */
class CommentInterceptor extends EntranceInterceptor {
  /**
    * The apply function is to supplement the information of the incoming parameter task, making the content of this task more complete.
   * Additional information includes: database information supplement, custom variable substitution, code check, limit limit, etc.
    * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。
    * 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
    *
    * @param task
    * @param logAppender Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
    * @return
    */
  override def apply(task: Task, logAppender: lang.StringBuilder): Task = task match {
    case requestPersistTask:RequestPersistTask => requestPersistTask.getRunType match {
      case "sql" | "hql"  => requestPersistTask.setExecutionCode(SQLCommentHelper.dealComment(requestPersistTask.getExecutionCode))
      case "python" | "py" => requestPersistTask.setExecutionCode(PythonCommentHelper.dealComment(requestPersistTask.getExecutionCode))
      case "scala" | "java" => requestPersistTask.setExecutionCode(ScalaCommentHelper.dealComment(requestPersistTask.getExecutionCode))
      case "sh" | "shell" =>
      case _ => requestPersistTask.setExecutionCode(SQLCommentHelper.dealComment(requestPersistTask.getExecutionCode))
    }
      requestPersistTask
    case _ => task
  }
}

trait CommentHelper{
  val commentPattern:Regex
  def dealComment(code:String):String
}

object SQLCommentHelper extends CommentHelper {
  override val commentPattern: Regex = """\s*--.+\s*""".r.unanchored
  private val comment = "(?ms)([\"']*['\"])|--.*?$|/\\*.*?\\*/"
  private val logger:Logger = LoggerFactory.getLogger(getClass)
  override def dealComment(code: String): String = {
    try{
      val p = Pattern.compile(comment)
      val sql = p.matcher(code).replaceAll("$1")
      sql
    }catch{
      case e:Exception => logger.warn("sql comment failed")
        code
      case t:Throwable => logger.warn("sql comment failed")
        code
    }
  }
}

object PythonCommentHelper extends CommentHelper{
  override val commentPattern: Regex = """^\s*#.+\s*""".r.unanchored
  val pythonCommentPattern:String = "(?ms)([\"'](?:|[^'])*['\"])|#.*?$|/\\*.*?\\*/"
  override def dealComment(code: String): String = {
    code
  }
}


object ScalaCommentHelper extends CommentHelper{
  override val commentPattern: Regex = """^\s*//.+\s*""".r.unanchored
  private val scalaCommentPattern:String = "(?ms)([\"'](?:|[^'])*['\"])|//.*?$|/\\*.*?\\*/"
  override def dealComment(code: String): String = {
    val p = Pattern.compile(scalaCommentPattern)
    p.matcher(code).replaceAll("$1")
  }
}


object CommentMain{
  def main(args: Array[String]): Unit = {
    val sqlCode = "select * from default.user;--你好;show tables"
    val sqlCode1 = "select * from default.user--你好;show tables"
    println(SQLCommentHelper.dealComment(sqlCode))
  }
}

