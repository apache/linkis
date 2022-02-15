/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.entrance.interceptor.impl

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.interceptor.exception.VarSubstitutionException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.protocol.variable.{RequestQueryAppVariable, RequestQueryGlobalVariable, ResponseQueryVariable}
import org.apache.linkis.rpc.Sender
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateUtils
import org.apache.linkis.common.utils.{Logging, Utils}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception._


object CustomVariableUtils extends Logging {
  //hql sql jdbc to sql python to py
  private val SQL_TYPE = "sql"
  private val PY_TYPE = "python"
  private val JAVA_TYPE: String = "java"
  private val SCALA_TYPE: String = "scala"
  private val R_TYPE: String = "r"
  private val RUN_DATE = "run_date"
  private val RUN_TODAY = "run_today"
  private val TEAM: String = "team"

  /**
    * date Format
    */
  private val dateFormat = new SimpleDateFormat("yyyyMMdd")
  private val dateFormat_std = new SimpleDateFormat("yyyy-MM-dd")

  private val codeReg = "\\$\\{\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*[\\+\\-\\*/]?\\s*[A-Za-z0-9_\\.]*\\s*\\}".r

  /**
    * calculation Reg
    * Get custom variables, if it is related to the left and right variables of the analytical calculation of the calculation
    */
  private val calReg = "(\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*)([\\+\\-\\*/]?)(\\s*[A-Za-z0-9_\\.]*\\s*)".r


  /**
    * replace custom variable
    * 1. Get the user-defined variable from the code and replace it
    *   * 2. If 1 is not done, then get the user-defined variable from args and replace it.
    *   * 3. If 2 is not done, get the user-defined variable from the console and replace it.
    *
    * 1. 从代码中得到用户定义的变量，进行替换
    * 2. 如果1没有做，那么从args中得到用户定义的变量，进行替换
    * 3. 如果2没有做，从CS中得到用户定义的变量，进行替换
    *3. 如果3没有做，从控制台中得到用户定义的变量，进行替换
    *
    * @param jobRequest : requestPersistTask
    * @return
    */
  def replaceCustomVar(jobRequest: JobRequest, runType: String): (Boolean, String) = {
    val code: String = jobRequest.getExecutionCode
    var codeType = SQL_TYPE
    runType match {
      case "hql" | "sql" | "jdbc" | "hive"| "psql" => codeType = SQL_TYPE
      case "python" | "py" => codeType = PY_TYPE
      case "java" => codeType = JAVA_TYPE
      case "scala" => codeType = SCALA_TYPE
      case "sh" | "shell" => codeType = SQL_TYPE
      case _ => return (false, code)
    }

    var run_date: CustomDateType = null
    var run_today: CustomDateType = null
    val nameAndType = mutable.Map[String, VariableType]()
    val nameAndValue: mutable.Map[String, String] = getCustomVar(code, codeType)

    def putNameAndType(data: mutable.Map[String, String]): Unit = if (null != data) data foreach {
      case (key, value) => key match {
        case RUN_DATE => if (nameAndType.get(RUN_DATE).isEmpty) {
          val run_date_str = value.asInstanceOf[String]
          if (StringUtils.isNotEmpty(run_date_str)) {
            run_date = new CustomDateType(run_date_str, false)
            nameAndType(RUN_DATE) = DateType(run_date)
          }
        }
        case RUN_TODAY => if (nameAndType.get(RUN_TODAY).isEmpty) {
          val run_today_str = value.asInstanceOf[String]
          if (StringUtils.isNotEmpty(run_today_str)) {
            run_today = new CustomDateType(run_today_str, false)
            nameAndType(RUN_TODAY) = DateType(run_today)
          }
        }
        case _ => if (nameAndType.get(key).isEmpty && StringUtils.isNotEmpty(value)) {
            if ((allCatch opt value.toDouble).isDefined) {
              nameAndType(key) = DoubleValue(value.toDouble)
            } else {
              nameAndType(key) = StringType(value)
            }
        }
      }
    }

    //The first step is to replace the variable from code
    //第一步来自code的变量替换
    putNameAndType(nameAndValue)


    /* Perform the second step to replace the parameters passed in args*/
    /* 进行第二步，对args传进的参数进行替换*/
    val variableMap = TaskUtils.getVariableMap(jobRequest.getParams.asInstanceOf[util.Map[String, Any]])
      .map { case (k, v) => k -> v.asInstanceOf[String] }
    putNameAndType(variableMap)

    /* Go to the four step and take the user's parameters to the linkis-ps-publicservice module.*/
    /*进行第四步，向linkis-ps-publicservice模块去拿用户的参数*/
    val sender = Sender.getSender(Configuration.CLOUD_CONSOLE_VARIABLE_SPRING_APPLICATION_NAME.getValue)

    val umUser: String = jobRequest.getSubmitUser
    val codeTypeFromLabel = LabelUtil.getCodeType(jobRequest.getLabels)
    val userCreator = LabelUtil.getUserCreator(jobRequest.getLabels)
    val creator: String = if (null != userCreator) userCreator._2 else null
    val requestProtocol = if (null != userCreator) {
      RequestQueryAppVariable(umUser, creator, codeTypeFromLabel)
    } else {
      RequestQueryGlobalVariable(umUser)
    }
    val response: ResponseQueryVariable = Utils.tryAndWarn(sender.ask(requestProtocol).asInstanceOf[ResponseQueryVariable])
    if (null != response) {
      val keyAndValue = response.getKeyAndValue
      val keyAndValueScala: mutable.Map[String, String] = keyAndValue
      putNameAndType(keyAndValueScala)
    }

    /*The last step, if you have not set run_date, then it is the default */
    /*最后一步，如果都没有设置run_date，那么就是默认*/
    if (nameAndType.get(RUN_DATE).isEmpty || null == run_date){
      run_date = new CustomDateType(getYesterday(false), false)
      nameAndType(RUN_DATE) = DateType(new CustomDateType(run_date.toString, false))
    }
    nameAndType("run_date_std") = DateType(new CustomDateType(run_date.getStdDate))
    nameAndType("run_month_begin") = MonthType(new CustomMonthType(run_date.toString, false))
    nameAndType("run_month_begin_std") = MonthType(new CustomMonthType(run_date.toString))
    nameAndType("run_month_end") = MonthType(new CustomMonthType(run_date.toString, false, true))
    nameAndType("run_month_end_std") = MonthType(new CustomMonthType(run_date.toString, true, true))


    /*
    Variables based on run_today
    */
    if (nameAndType.get(RUN_TODAY).isEmpty || null == run_today) {
      run_today = new CustomDateType(getToday(false), false)
      nameAndType(RUN_TODAY) = DateType(new CustomDateType(run_today.toString, false))
    }
    nameAndType("run_today_std") = DateType(new CustomDateType(run_today.getStdDate))
    nameAndType("run_month_now_begin") = MonthType(new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, false))
    nameAndType("run_month_now_begin_std") = MonthType(new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1))
    nameAndType("run_month_now_end") = MonthType(new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, false, true))
    nameAndType("run_month_now_end_std") = MonthType(new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, true, true))

    if (nameAndType.get("user").isEmpty){
      nameAndType("user") = StringType(jobRequest.getSubmitUser)
    }
    (true, parserVar(code, nameAndType))
  }


  /**
    * Parse and replace the value of the variable
    * 1.Get the expression and calculations
    * 2.Print user log
    * 3.Assemble code
    *
    * @param code        :code
    * @param nameAndType : variable name and Type
    * @return
    */
  def parserVar(code: String, nameAndType: mutable.Map[String, VariableType]): String = {
    val parseCode = new StringBuilder
    val codes = codeReg.split(code)
    val expressionCache = mutable.HashSet[String]()

    var i = 0

    codeReg.findAllIn(code).foreach(str => {
      calReg.findFirstMatchIn(str).foreach(ma => {
        i = i + 1
        /**
          * name left value
          * rightValue right value
          * signal: + - * /
          */
        val name = ma.group(1)
        val signal = ma.group(2)
        val rightValue = ma.group(3)

        if (name == null || name.trim.isEmpty) {
          throw  VarSubstitutionException(20041,s"[$str] replaced var is null")
        } else {
          var expression = name.trim
          val varType = nameAndType.get(name.trim).orNull
          if (varType == null) {
            logger.warn(s"Use undefined variables or use the set method: [$str](使用了未定义的变量或者使用了set方式:[$str])")
            parseCode ++= codes(i - 1) ++ str
          } else {
            var res: String = varType.getValue
            if (signal != null && !signal.trim.isEmpty) {
              if (rightValue == null || rightValue.trim.isEmpty) {
                throw VarSubstitutionException(20042, s"[$str] expression is not right, please check")
              } else {
                expression = expression + "_" + signal.trim + "_" + rightValue.trim
                val rightToken = rightValue.trim
                val rightRes = if (nameAndType.contains(rightToken)) {
                  nameAndType(rightToken).getValue
                } else {
                  rightToken
                }
                res = varType.calculator(signal.trim, rightRes)
              }
            }
            if (!expressionCache.contains(expression)) {
              logger.info(s"Variable expression [$str] = $res(变量表达式[$str] = $res)")
              //println(s"变量表达式[$str] = $res")
              expressionCache += expression
            }
            //println(s"变量表达式序号:$i\t[$str] = $res")
            parseCode ++= codes(i - 1) ++ res
          }
        }
      })
    })
    if (i == codes.length - 1) {
      parseCode ++= codes(i)
    }
    val parsedCode = deleteUselessSemicolon(parseCode)
    org.apache.commons.lang.StringUtils.strip(parsedCode)
    //   Utils.trimBlank()
  }

  private def deleteUselessSemicolon(code:mutable.StringBuilder): String ={
    val tempStr = code.toString()
    val arr = new ArrayBuffer[String]()
    tempStr.split(";").filter(StringUtils.isNotBlank).foreach(arr += _)
    arr.mkString(";")
  }


  def replaceTeamParams(code:String, teamParams:java.util.Map[String,java.util.List[String]]):String = {
    if (StringUtils.isEmpty(code)) return code
    val tempParams:mutable.Map[String, VariableType] = mutable.Map[String, VariableType]()
    import scala.collection.JavaConversions._
    teamParams foreach {
      case (key, value) => logger.info("teamParams key is {}", key)
        value foreach (v => logger.info("value is {}", v))
    }
    teamParams foreach {
      case (key, value) => val f_value = value.get(0)
        tempParams(key) = StringType(f_value)
    }
    parserVar(code, tempParams)
  }


  /**
    * Get user-defined variables and values
    *
    * @param code     :code
    * @param codeType :SQL,PYTHON
    * @return
    */
  def getCustomVar(code: String, codeType: String): mutable.Map[String, String] = {
    val nameAndValue = mutable.Map[String, String]()

    var varString:String = null
    var errString:String = null

    codeType match {
      case SQL_TYPE => varString = """\s*--@set\s*.+\s*"""
        errString = """\s*--@.*"""
      case PY_TYPE => varString = """\s*#@set\s*.+\s*"""
        errString = """\s*#@"""
      case SCALA_TYPE => varString = """\s*//@set\s*.+\s*"""
        errString = """\s*//@.+"""
      case JAVA_TYPE => varString = """\s*!!@set\s*.+\s*"""
    }

    val customRegex = varString.r.unanchored
    val errRegex = errString.r.unanchored
    code.split("\n").foreach { str => {
      str match {
        case customRegex() =>
          val clearStr = if (str.endsWith(";")) str.substring(0, str.length - 1) else str
          val res: Array[String] = clearStr.split("=")
          if (res != null && res.length == 2) {
            val nameSet = res(0).split("@set")
            if (nameSet != null && nameSet.length == 2) {
              val name = nameSet(1).trim
              //if (nameAndValue.getOrElse(name, null) == null) {
              nameAndValue(name) = res(1).trim
             // } else {
              //  throw  VarSubstitutionException(20043, s"$name is defined repeatedly")
             // }
            }
          } else {
            if (res.length > 2) {
              throw  VarSubstitutionException(20044, s"$str var defined uncorrectly")
            } else {
              throw  VarSubstitutionException(20045, s"var was defined uncorrectly:$str")
            }
          }
        case errRegex() =>
          warn(s"The variable definition is incorrect: $str , if it is not used, it will not run the error, but it is recommended to use the correct specification to define(变量定义有误：$str ,如果没有使用该变量将不会运行错误，但建议使用正确的规范进行定义)")
        case _ =>
      }
    }
    }
    nameAndValue
  }

  /**
    * Get Today"s date
    *
    * @param std :2017-11-16
    * @return
    */
  def getToday(std: Boolean = true): String = {
    val cal: Calendar = Calendar.getInstance()
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }
  /**
    * Get Yesterday"s date
    *
    * @param std :2017-11-16
    * @return
    */
  def getYesterday(std: Boolean = true): String = {
    val cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }

  /**
    * Get Month"s date
    *
    * @param std   :2017-11-01
    * @param isEnd :01 or 30,31
    * @return
    */
  def getMonth(std: Boolean = true, isEnd: Boolean = false, date: Date): String = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.set(Calendar.DATE, 1)
    if (isEnd) {
      cal.roll(Calendar.DATE, -1)
    }
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }

  /**
    * get 1st day or last day of a Quarter
    * @param std
    * @param isEnd
    * @param date
    * @return
    */
  def getQuarter(std: Boolean = true, isEnd: Boolean = false, date: Date): String = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.set(Calendar.DATE, 1)
    val monthDigit: Int = cal.get(Calendar.MONTH)//get method with MONTH field returns 0-11
    if (0<= monthDigit && monthDigit <= 2) {
      cal.set(Calendar.MONTH, 0)
    } else if (3<= monthDigit && monthDigit <= 5) {
      cal.set(Calendar.MONTH, 3)
    } else if (6<= monthDigit && monthDigit <= 8) {
      cal.set(Calendar.MONTH, 6)
    } else if (9<= monthDigit && monthDigit <= 11) {
      cal.set(Calendar.MONTH, 9)
    }
    if (isEnd) {
      cal.add(Calendar.MONTH, 2)
      cal.roll(Calendar.DATE, -1)
    }
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }

  /**
    * get 1st day or last day of a HalfYear
    * @param std
    * @param isEnd
    * @param date
    * @return
    */
  def getHalfYear(std: Boolean = true, isEnd: Boolean = false, date: Date): String = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.set(Calendar.DATE, 1)
    val monthDigit: Int = cal.get(Calendar.MONTH) //get method with MONTH field returns 0-11
    if (0<= monthDigit && monthDigit <= 5) {
      cal.set(Calendar.MONTH, 0)
    } else if (6<= monthDigit && monthDigit <= 11) {
      cal.set(Calendar.MONTH, 6)
    }
    if (isEnd) {
      cal.add(Calendar.MONTH, 5)
      cal.roll(Calendar.DATE, -1)
    }
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }

  /**
    * get 1st day or last day of a year
    * @param std
    * @param isEnd
    * @param date
    * @return
    */
  def getYear(std: Boolean = true, isEnd: Boolean = false, date: Date): String = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.set(Calendar.DATE, 1)
    cal.set(Calendar.MONTH, 0) // set methods with field MONTH accepts 0-11
    if (isEnd) {
      cal.add(Calendar.MONTH, 11)
      cal.roll(Calendar.DATE, -1)
    }
    if (std) {
      dateFormat_std.format(cal.getTime)
    } else {
      dateFormat.format(cal.getTime)
    }
  }

  //
  //  def main(args: Array[String]): Unit = {
  //    val code = "--@set a=1\n--@set b=2\nselect ${a +2},${a   + 1},${a},${a },${b},${b}"
  //    val task = new RequestPersistTask
  //    val args:java.util.Map[String, Object] = new util.HashMap[String, Object]()
  //    args.put(RUN_DATE, "20181030")
  //    task.setExecutionCode(code)
  //    task.setParams(args)
  //    val str = replaceCustomVar(task, "sql")
  //    println(str)
  //
  //    println("************code**************")
  //    var preSQL = ""
  //    var endSQL = ""
  //    var sql = "select * from (select * from utf_8_test limit 20) t ;"
  //    if (sql.contains("limit")) {
  //      preSQL = sql.substring(0, sql.lastIndexOf("limit")).trim
  //      endSQL = sql.substring(sql.lastIndexOf("limit")).trim
  //    } else if (sql.contains("LIMIT")) {
  //      preSQL = sql.substring(0, sql.lastIndexOf("limit")).trim
  //      endSQL = sql.substring(sql.lastIndexOf("limit")).trim
  //    }
  //    println(preSQL)
  //    println(endSQL)
  //    /* val yestd = new CustomDateType("2017-11-11",false)
  //     println(yestd)*/
  //  }

}

trait VariableType {
  def getValue: String

  def calculator(signal: String, bValue: String): String
}

case class DateType(value: CustomDateType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ => throw VarSubstitutionException(20046, s"Date class is not supported to uss：${signal}")
    }
  }
}

case class MonthType(value: CustomMonthType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ => throw VarSubstitutionException(20046, s"Date class is not supported to uss：${signal}")
    }
  }
}

case class QuarterType(value: CustomQuarterType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ => throw VarSubstitutionException(20046,s"Date class is not supported to uss：${signal}")
    }
  }
}

case class HalfYearType(value: CustomHalfYearType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ => throw VarSubstitutionException(20046,s"Date class is not supported to uss：${signal}")
    }
  }
}

case class YearType(value: CustomYearType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ => throw VarSubstitutionException(20046,s"Date class is not supported to uss：${signal}")
    }
  }
}

case class LongType(value: Long) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => val res = value + bValue.toLong; res.toString
      case "-" => val res = value - bValue.toLong; res.toString
      case "*" => val res = value * bValue.toLong; res.toString
      case "/" => val res = value / bValue.toLong; res.toString
      case _ => throw VarSubstitutionException(20047, s"Int class is not supported to uss：${signal}")
    }
  }
}

case class DoubleValue(value: Double) extends VariableType {
  override def getValue: String = doubleOrLong(value).toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => val res = value + bValue.toDouble; doubleOrLong(res).toString
      case "-" => val res = value - bValue.toDouble; doubleOrLong(res).toString
      case "*" => val res = value * bValue.toDouble; doubleOrLong(res).toString
      case "/" => val res = value / bValue.toDouble; doubleOrLong(res).toString
      case _ => throw VarSubstitutionException(20047, s"Double class is not supported to uss：${signal}")
    }
  }

  private def doubleOrLong(d: Double): AnyVal = {
    if (d.asInstanceOf[Long] == d) d.asInstanceOf[Long] else d
  }

}


case class FloatType(value: Float) extends VariableType {
  override def getValue: String = floatOrLong(value).toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => val res = value + bValue.toFloat; floatOrLong(res).toString
      case "-" => val res = value - bValue.toFloat; floatOrLong(res).toString
      case "*" => val res = value * bValue.toFloat; floatOrLong(res).toString
      case "/" => val res = value / bValue.toLong; floatOrLong(res).toString
      case _ => throw VarSubstitutionException(20048, s"Float class is not supported to use：${signal}")
    }
  }

  private def floatOrLong(f: Float): AnyVal = {
    if (f.asInstanceOf[Long] == f) f.asInstanceOf[Long] else f
  }

}

case class StringType(value: String) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue
      case _ => throw VarSubstitutionException(20049, s"String class is not supported to uss：${signal}")
    }
  }
}


class CustomDateType(date: String, std: Boolean = true) {

  val dateFormat = new SimpleDateFormat("yyyyMMdd")
  val dateFormat_std = new SimpleDateFormat("yyyy-MM-dd")

  def -(days: Int): String = {
    if (std) {
      dateFormat_std.format(DateUtils.addDays(dateFormat_std.parse(date), -days))
    } else {
      dateFormat.format(DateUtils.addDays(dateFormat.parse(date), -days))
    }
  }

  def +(days: Int): String = {
    if (std) {
      dateFormat_std.format(DateUtils.addDays(dateFormat_std.parse(date), days))
    } else {
      dateFormat.format(DateUtils.addDays(dateFormat.parse(date), days))
    }
  }

  def getDate: Date = {
    if (std) {
      dateFormat_std.parse(date)
    } else {
      dateFormat.parse(date)
    }
  }

  def getStdDate: String = {
    if (std) {
      dateFormat_std.format(dateFormat_std.parse(date))
    } else {
      dateFormat_std.format(dateFormat.parse(date))
    }
  }

  override def toString: String = {
    if (std) {
      dateFormat_std.format(dateFormat_std.parse(date))
    } else {
      dateFormat.format(dateFormat.parse(date))
    }
  }
}

class CustomMonthType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  val dateFormat = new SimpleDateFormat("yyyyMMdd")

  def -(months: Int): String = {
    if (std) {
      CustomVariableUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    } else {
      CustomVariableUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    }
  }

  def +(months: Int): String = {
    if (std) {
      CustomVariableUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    } else {
      CustomVariableUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    }
  }

  override def toString: String = {
    if (std) {
      CustomVariableUtils.getMonth(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      CustomVariableUtils.getMonth(std, isEnd, v)
    }
  }

}


/*
 Given a Date, convert into Quarter
 */
class CustomQuarterType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  val dateFormat = new SimpleDateFormat("yyyyMMdd")

  def getCurrentQuarter(date: String) = {
    dateFormat.parse(CustomVariableUtils.getQuarter(false, false, dateFormat.parse(date)))
  }

  def -(quarters: Int): String = {
    CustomVariableUtils.getQuarter(std, isEnd, DateUtils.addMonths(getCurrentQuarter(date), -quarters * 3))
  }

  def +(quarters: Int): String = {
    CustomVariableUtils.getQuarter(std, isEnd, DateUtils.addMonths(getCurrentQuarter(date), quarters * 3))
  }

  override def toString: String = {
    if (std) {
      CustomVariableUtils.getQuarter(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      CustomVariableUtils.getQuarter(std, isEnd, v)
    }
  }

}

/*
 Given a Date, convert into HalfYear
 */
class CustomHalfYearType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  val dateFormat = new SimpleDateFormat("yyyyMMdd")

  def getCurrentHalfYear(date: String) = {
    dateFormat.parse(CustomVariableUtils.getHalfYear(false, false, dateFormat.parse(date)))
  }

  def -(halfYears: Int): String = {
    CustomVariableUtils.getHalfYear(std, isEnd, DateUtils.addMonths(getCurrentHalfYear(date), -halfYears * 6))
  }

  def +(halfYears: Int): String = {
    CustomVariableUtils.getHalfYear(std, isEnd, DateUtils.addMonths(getCurrentHalfYear(date), halfYears * 6))
  }

  override def toString: String = {
    if (std) {
      CustomVariableUtils.getHalfYear(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      CustomVariableUtils.getHalfYear(std, isEnd, v)
    }
  }

}

/*
 Given a Date convert into Year
 */
class CustomYearType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  val dateFormat = new SimpleDateFormat("yyyyMMdd")

  def -(years: Int): String = {
    CustomVariableUtils.getYear(std, isEnd, DateUtils.addYears(dateFormat.parse(date), -years))
  }

  def +(years: Int): String = {
    CustomVariableUtils.getYear(std, isEnd, DateUtils.addYears(dateFormat.parse(date), years))
  }

  override def toString: String = {
    if (std) {
      CustomVariableUtils.getYear(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      CustomVariableUtils.getYear(std, isEnd, v)
    }
  }

}
