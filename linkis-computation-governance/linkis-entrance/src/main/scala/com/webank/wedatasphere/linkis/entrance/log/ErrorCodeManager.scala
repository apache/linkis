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

package com.webank.wedatasphere.linkis.entrance.log

import java.io.{BufferedReader, InputStreamReader}
import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.storage.FSFactory
import javax.annotation.PostConstruct

import scala.collection.mutable.ArrayBuffer


abstract class ErrorCodeManager {

  def getErrorCodes: Array[ErrorCode]

  def errorMatch(log: String): Option[(String, String)] = {
    getErrorCodes.foreach(e => if(e.regex.findFirstIn(log).isDefined) {
      val matched = e.regex.unapplySeq(log)
      if(matched.nonEmpty)
        return Some(e.code -> e.message.format(matched.get:_*))
      else Some(e.code -> e.message)
    })
    None
  }
}

abstract class FileErrorCodeManager extends ErrorCodeManager with Logging {
  override def getErrorCodes: Array[ErrorCode] = (new ArrayBuffer[ErrorCode]() ++= getCommonErrorCodes ++= getErrorCodesDynamic).toArray

  val errorCodeFileDir: String = {
    val specifiedDir = EntranceConfiguration.ERROR_CODE_FILE_DIR.getValue
    if (specifiedDir.endsWith("/")) specifiedDir else specifiedDir + "/"
  }

  val errorCodeFile: String
  private val user = EntranceConfiguration.ENTRANCE_USER.getValue
  private val fileSystem = FSFactory.getFs(new FsPath(errorCodeFileDir))

  @PostConstruct
  def init(): Unit = {
    Utils.tryAndWarn(fileSystem.init(new util.HashMap[String, String]()))
  }

  val dynamicErrorCodes: ArrayBuffer[ErrorCode] = new ArrayBuffer[ErrorCode]()

  Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      logger.info("start to get error code properties from {}", errorCodeFile)
      dynamicErrorCodes.clear()
      val bufferedReader = new BufferedReader(new InputStreamReader(fileSystem.read(new FsPath(errorCodeFile)), "utf-8"))
      var line: String = null
      while ( {
        line = bufferedReader.readLine(); line != null
      }) {
        val arr = line.split(",")
        if (arr.length < 3){
          logger.warn("errorcode line {} format is not correct", line)
        }else{
          dynamicErrorCodes += ErrorCode(arr(0).r.unanchored, arr(1), arr(2))
        }
      }
    }
  }, 0, 1,TimeUnit.HOURS)


  def getErrorCodesDynamic:Array[ErrorCode] = dynamicErrorCodes.toArray

  def getCommonErrorCodes:Array[ErrorCode] = {
    Array(ErrorCode("queue (\\S+) is not exists in YARN".r.unanchored, "10001", "会话创建失败，%s队列不存在，请检查队列设置是否正确"),
      ErrorCode("User (\\S+) cannot submit applications to queue (\\S+)".r.unanchored, "10001", "会话创建失败，用户%s不能提交应用到队列：%s，请检查队列设置是否正确"),
      ErrorCode("您本次向任务队列（[a-zA-Z_0-9\\.]+）请求资源（(.+)），任务队列最大可用资源（.+），任务队列剩余可用资源（(.+)）您已占用任务队列资源（.+）".r.unanchored, "20001", "Session创建失败，当前申请资源%s，队列可用资源%s,请检查资源配置是否合理"),
      ErrorCode("远程服务器没有足够资源实例化[a-zA-Z]+ Session，通常是由于您设置【驱动内存】或【客户端内存】过高导致的，建议kill脚本，调低参数后重新提交！等待下次调度...".r.unanchored, "20002", "Session创建失败，服务器资源不足，请稍后再试"),
      ErrorCode("request resources from ResourceManager has reached 560++ tries, give up and mark it as FAILED.".r.unanchored, "20003", "Session创建失败，队列资源不足，请稍后再试"),
      ErrorCode("Caused by:\\s*java.io.FileNotFoundException".r.unanchored, "20003", "文件%s不存在"),
      ErrorCode("OutOfMemoryError".r.unanchored, "20083", "Java进程内存溢出"),
      ErrorCode("Permission denied:\\s*user=[a-zA-Z0-9_]+,\\s*access=[A-Z]+\\s*,\\s*inode=\"([a-zA-Z0-9/_\\.]+)\"".r.unanchored, "30001", "%s无权限访问，请申请开通数据表权限"),
      ErrorCode("Database '([a-zA-Z_0-9]+)' not found".r.unanchored, "40001", "数据库%s不存在，请检查引用的数据库是否有误"),
      ErrorCode("Database does not exist: ([a-zA-Z_0-9]+)".r.unanchored, "40001", "数据库%s不存在，请检查引用的数据库是否有误"),
      ErrorCode("Table or view not found: ([`\\.a-zA-Z_0-9]+)".r.unanchored, "40002", "表%s不存在，请检查引用的表是否有误"),
      ErrorCode("Table not found '([a-zA-Z_0-9]+)'".r.unanchored, "40002", "表%s不存在，请检查引用的表是否有误"),
      ErrorCode("cannot resolve '`(.+)`' given input columns".r.unanchored, "40003", "字段%s不存在，请检查引用的字段是否有误"),
      ErrorCode(" Invalid table alias or column reference '(.+)':".r.unanchored, "40003", "字段%s不存在，请检查引用的字段是否有误"),
      ErrorCode("([a-zA-Z_0-9]+) is not a valid partition column in table ([`\\.a-zA-Z_0-9]+)".r.unanchored, "40004", "分区字段%s不存在，请检查引用的表%s是否为分区表或分区字段有误"),
      ErrorCode("Partition spec \\{(\\S+)\\} contains non-partition columns".r.unanchored, "40004", "分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误"),
      ErrorCode("table is not partitioned but partition spec exists:\\{(.+)\\}".r.unanchored, "40004", "分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误"),
      ErrorCode("extraneous input '\\)'".r.unanchored, "50001", "括号不匹配，请检查代码中括号是否前后匹配"),
      ErrorCode("missing EOF at '\\)'".r.unanchored, "50001", "括号不匹配，请检查代码中括号是否前后匹配"),
      ErrorCode("expression '(\\S+)' is neither present in the group by".r.unanchored, "50002", "非聚合函数%s必须写在group by中，请检查代码的group by语法"),
      ErrorCode("grouping expressions sequence is empty,\\s?and '(\\S+)' is not an aggregate function".r.unanchored, "50002", "非聚合函数%s必须写在group by中，请检查代码的group by语法"),
      ErrorCode("Expression not in GROUP BY key '(\\S+)'".r.unanchored, "50002", "非聚合函数%s必须写在group by中，请检查代码的group by语法"),
      ErrorCode("Undefined function: '(\\S+)'".r.unanchored, "50003", "未知函数%s，请检查代码中引用的函数是否有误"),
      ErrorCode("Invalid function '(\\S+)'".r.unanchored, "50003", "未知函数%s，请检查代码中引用的函数是否有误"),
      ErrorCode("Reference '(\\S+)' is ambiguous".r.unanchored, "50004", "字段%s存在名字冲突，请检查子查询内是否有同名字段"),
      ErrorCode("Ambiguous column Reference '(\\S+)' in subquery".r.unanchored, "50004", "字段%s存在名字冲突，请检查子查询内是否有同名字段"),
      ErrorCode("Column '(\\S+)' Found in more than One Tables/Subqueries".r.unanchored, "50005", "字段%s必须指定表或者子查询别名，请检查该字段来源"),
      ErrorCode("Table or view '(\\S+)' already exists in database '(\\S+)'".r.unanchored, "50006", "表%s在数据库%s中已经存在，请删除相应表后重试"),
      ErrorCode("Table (\\S+) already exists".r.unanchored, "50006", "表%s在数据库中已经存在，请删除相应表后重试"),
      ErrorCode("Table already exists".r.unanchored, "50006", "表%s在数据库中已经存在，请删除相应表后重试"),
      ErrorCode("""AnalysisException: (\S+) already exists""".r.unanchored, "50006", "表%s在数据库中已经存在，请删除相应表后重试"),
      ErrorCode("java.io.FileNotFoundException: (\\S+) \\(No such file or directory\\)".r.unanchored,"64001","找不到导入文件地址：%s"),
      ErrorCode("java.io.IOException: Permission denied(.+)at org.apache.poi.xssf.streaming.SXSSFWorkbook.createAndRegisterSXSSFSheet".r.unanchored,"64002","导出为excel时临时文件目录权限异常"),
      ErrorCode("java.io.IOException: Mkdirs failed to create (\\S+) (.+)".r.unanchored,"64003","导出文件时无法创建目录：%s"),
      ErrorCode("""ImportError: No module named (\S+)""".r.unanchored, "50008", "导入模块错误，系统没有%s模块，请联系运维人员安装"),
      ErrorCode("""requires that the data to be inserted have the same number of columns as the target table""".r.unanchored, "50009",
        "插入目标表字段数量不匹配,请检查代码！"),
      ErrorCode("""missing \) at '(\S+)' near '<EOF>'""".r.unanchored, "50010", "%s处括号不匹配，请检查代码！"),
      ErrorCode("""due to data type mismatch: differing types in""".r.unanchored,
        "50011", "数据类型不匹配，请检查代码！"),
      ErrorCode("""Invalid column reference (\S+)""".r.unanchored, "50012", "字段%s引用有误，请检查字段是否存在！"),
      ErrorCode("""Can't extract value from (\S+): need struct type but got string""".r.unanchored, "50013", "字段%s提取数据失败"),
      ErrorCode("""mismatched input '(\S+)' expecting""".r.unanchored, "50014", "括号或者关键字不匹配，请检查代码！"),
      ErrorCode("""GROUP BY position (\S+) is not in select list""".r.unanchored, "50015", "group by 位置2不在select列表中，请检查代码！"),
      ErrorCode("""'NoneType' object""".r.unanchored, "50016", "代码中存在NoneType空类型变量，请检查代码"),
      ErrorCode("""IndexError:List index out of range""".r.unanchored, "50017", "数组越界"),
      ErrorCode("""Can't extract value from (\S+): need struct type but got string""".r.unanchored, "50013", "字段提取数据失败请检查字段类型"),
      ErrorCode("""Cannot insert into target table because column number/types are different '(\S+)'""".r.unanchored, "50018", "插入数据未指定目标表字段%s，请检查代码！"),
      ErrorCode("""Invalid table alias '(\S+)'""".r.unanchored, "50019", "表别名%s错误，请检查代码！"),
      ErrorCode("""UDFArgumentException Argument expected""".r.unanchored, "50020", "UDF函数未指定参数，请检查代码！"),
      ErrorCode("""aggregate functions are not allowed in GROUP BY""".r.unanchored, "50021", "聚合函数%s不能写在group by 中，请检查代码！"),
      ErrorCode("SyntaxError".r.unanchored, "50007", "您的代码有语法错误，请您修改代码之后执行"),
      ErrorCode("""Table not found""".r.unanchored, "40002", "表不存在，请检查引用的表是否有误"),
      ErrorCode("""No matching method""".r.unanchored, "40003", "函数使用错误，请检查您使用的函数方式"),
      ErrorCode("""is killed by user""".r.unanchored, "50032", "用户主动kill任务"),
      ErrorCode("""name '(\S+)' is not defined""".r.unanchored, "60001", "python代码变量%s未定义"),
      ErrorCode("""Undefined function:\s+'(\S+)'""".r.unanchored, "60002", "python udf %s 未定义"),
      ErrorCode("FAILED: ParseException".r.unanchored, "50007", "您的sql代码可能有语法错误，请检查sql代码"),
      ErrorCode("org.apache.spark.sql.catalyst.parser.ParseException".r.unanchored,"50007", "您的sql代码可能有语法错误，请检查sql代码"),
      ErrorCode("""ParseException:""".r.unanchored, "60003", "脚本语法有误"),
      ErrorCode("""Permission denied""".r.unanchored, "60010", "您可能没有相关权限"),
      ErrorCode("""cannot concatenate '(\S+)' and '(\S+)'""".r.unanchored, "61027", "python执行不能将%s和%s两种类型进行连接"),
      ErrorCode("""Py4JJavaError: An error occurred""".r.unanchored, "60020", "pyspark执行失败，可能是语法错误或stage失败"),
      ErrorCode("""unexpected indent""".r.unanchored, "61028", "python代码缩进对齐有误"),
      ErrorCode("""is exceeded""".r.unanchored, "60078", "个人库超过限制"),
      ErrorCode("""unexpected indent""".r.unanchored, "69582", "python代码缩进有误"),
      ErrorCode("""unexpected character after line""".r.unanchored, "69583", "python代码反斜杠后面必须换行"),
      ErrorCode("""Invalid row number""".r.unanchored, "60091", "导出Excel表超过最大限制1048575"),
      ErrorCode("""parquet.io.ParquetDecodingException""".r.unanchored, "60092", "python save as table未指定格式，默认用parquet保存，hive查询报错"),
      ErrorCode("""errCode: 11011""".r.unanchored, "11011", "远程服务器内存资源不足"),
      ErrorCode("""errCode: 11012""".r.unanchored, "11012", "远程服务器CPU资源不足"),
      ErrorCode("""errCode: 11013""".r.unanchored, "11013", "远程服务器实例资源不足"),
      ErrorCode("""errCode: 11014""".r.unanchored, "11014", "队列CPU资源不足"),
      ErrorCode("""errCode: 11015""".r.unanchored, "11015", "队列内存资源不足"),
      ErrorCode("""errCode: 11016""".r.unanchored, "11016", "队列实例数超过限制"),
      ErrorCode("""errCode: 11017""".r.unanchored, "11017", "超出全局计算引擎实例限制"),
      ErrorCode("""资源不足""".r.unanchored, "60035", "资源不足，启动引擎失败"),
      ErrorCode("""获取Yarn队列信息异常""".r.unanchored, "60075", "获取Yarn队列信息异常,可能是您设置的yarn队列不存在")
    )
  }
}

/**
  * errorCodeManager的单例对象,主要是用来生成固定的错误码
  */
object FixedErrorCodeManager extends FileErrorCodeManager {

  override val errorCodeFile: String = ""

  override def getErrorCodes: Array[ErrorCode] = getCommonErrorCodes
}


object Main{
  def main(args: Array[String]): Unit = {
  }
}


/**
  * RefreshableErrorCodeManager corresponds to FixedErrorCodeManager, and refresheyeErrorCodeManager can update its own errorCodes through the query module.
 * The purpose is to enable users to update the error code at any time by modifying the database.
  * RefreshableErrorCodeManager 与 FixedErrorCodeManager 是对应的，refreshaleErrorCodeManager可以通过query模块进行更新自身的errorCodes
  * 目的是为了能够让用户通过修改数据库随时更新错误码
  */
//
//object RefreshableErrorCodeManager extends ErrorCodeManager{
//  private val sender:Sender =
//    Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue, 1000 * 60 * 60, 100)
//
//  private val logger:Logger = LoggerFactory.getLogger(getClass)
//  private var errorCodes:Array[ErrorCode] = _
//
//  class FetchErrorCodeThread extends Runnable{
//    override def run(): Unit = {
//      val requestErrorCode = new RequestErrorCode
//      val responseErrorCode = sender.send(requestErrorCode).asInstanceOf[ResponseErrorCode]
//      Utils.tryAndWarnMsg{
//        val responseErrorCode = sender.send(requestErrorCode).asInstanceOf[ResponseErrorCode]
//        val status = responseErrorCode.getStatus
//        val message = responseErrorCode.getMessage
//        if (status != 0){
//          logger.warn(s"Error encounters when retrieve errorCodes from query module, reason: $message")
//        }else{
//          val errorCodeList = responseErrorCode.getResult
//          val arrayBuffer = new ArrayBuffer[ErrorCode]()
//          import scala.collection.JavaConversions._
//          errorCodeList foreach { errorCode =>
//            val regex = errorCode.getErrorRegex.r.unanchored
//            val errorCode_ = errorCode.getErrorCode
//            val errorDesc = errorCode.getErrorDesc
//            arrayBuffer += ErrorCode(regex, errorCode_, errorDesc)
//          }
//          errorCodes = arrayBuffer.toArray
//        }
//      }("Query ErrorCodes failed. You may check the cause or just ignore it ")
//    }
//  }
//
//  override def getErrorCodes: Array[ErrorCode] = errorCodes
//}





