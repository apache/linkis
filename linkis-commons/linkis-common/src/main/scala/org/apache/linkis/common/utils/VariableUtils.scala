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
 
package org.apache.linkis.common.utils

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import org.apache.linkis.common.exception.LinkisCommonErrorException
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateUtils

import scala.collection.mutable
import scala.collection.convert.WrapAsScala._
import scala.util.matching.Regex


object VariableUtils extends Logging {

  val RUN_DATE = "run_date"
  /**
    * date Format
    */
  val dateFormat = new SimpleDateFormat("yyyyMMdd")
  val dateFormat_std = new SimpleDateFormat("yyyy-MM-dd")
  val codeReg: Regex = "\\$\\{\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*[\\+\\-\\*/]?\\s*[A-Za-z0-9_\\.]*\\s*\\}".r
  val calReg: Regex = "(\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*)([\\+\\-\\*/]?)(\\s*[A-Za-z0-9_\\.]*\\s*)".r

  def replace(replaceStr: String): String = replace(replaceStr, new util.HashMap[String, Any](0))

  def replace(replaceStr: String, variables: util.Map[String, Any]): String = {
    val nameAndType = mutable.Map[String, VariableType]()
    var run_date: CustomDateType = null
    variables foreach {
      case (RUN_DATE, value) if nameAndType.get(RUN_DATE).isEmpty =>
        val run_date_str = value.asInstanceOf[String]
        if (StringUtils.isNotEmpty(run_date_str)) {
          run_date = new CustomDateType(run_date_str, false)
          nameAndType(RUN_DATE) = DateType(run_date)
        }
      case (key, value: String) if !nameAndType.contains(key) && StringUtils.isNotEmpty(value) =>
        nameAndType(key) = Utils.tryCatch[VariableType](DoubleValue(value.toDouble))(_ => StringType(value))
      case _ =>
    }
    if(!nameAndType.contains(RUN_DATE) || null == run_date){
      run_date = new CustomDateType(getYesterday(false), false)
      nameAndType(RUN_DATE) = DateType(new CustomDateType(run_date.toString, false))
    }
    initAllDateVars(run_date, nameAndType)
    parserVar(replaceStr, nameAndType)
  }

  private def initAllDateVars(run_date: CustomDateType, nameAndType: mutable.Map[String, VariableType]): Unit = {
    val run_data_str = run_date.toString
    nameAndType("run_date_std") = DateType(new CustomDateType(run_date.getStdDate))
    nameAndType("run_month_begin") = DateType(new CustomMonthType(run_data_str, false))
    nameAndType("run_month_begin_std") = DateType(new CustomMonthType(run_data_str))
    nameAndType("run_month_end") = DateType(new CustomMonthType(run_data_str, false, true))
    nameAndType("run_month_end_std") = DateType(new CustomMonthType(run_data_str, true, true))
  }

  /**
    * Parse and replace the value of the variable
    * 1.Get the expression and calculations
    * 2.Print user log
    * 3.Assemble code
    *
    * @param replaceStr        : replaceStr
    * @param nameAndType : variable name and Type
    * @return
    */
  private def parserVar(replaceStr: String, nameAndType: mutable.Map[String, VariableType]): String = {
    val parseCode = new StringBuilder
    val codes = codeReg.split(replaceStr)
    var i = 0
    codeReg.findAllIn(replaceStr).foreach{ str =>
      i = i + 1
      calReg.findFirstMatchIn(str).foreach{ ma =>
        val name = ma.group(1)
        val signal = ma.group(2)
        val bValue = ma.group(3)
        if (StringUtils.isBlank(name)) {
          throw new LinkisCommonErrorException(20041,s"[$str] with empty variable name.")
        }
        val replacedStr = nameAndType.get(name.trim).map { varType =>
          if (StringUtils.isNotBlank(signal)) {
            if (StringUtils.isBlank(bValue)) {
              throw new LinkisCommonErrorException(20042, s"[$str] expression is not right, please check.")
            }
            varType.calculator(signal.trim, bValue.trim)
          } else varType.getValue
        }.getOrElse {
          warn(s"Use undefined variables or use the set method: [$str](使用了未定义的变量或者使用了set方式:[$str])")
          str
        }
        parseCode ++= codes(i - 1) ++ replacedStr
      }
    }
    if (i == codes.length - 1) {
      parseCode ++= codes(i)
    }
    StringUtils.strip(parseCode.toString)
  }

  /**
    * Get Yesterday"s date
    *
    * @param std :2017-11-16
    * @return
    */
  private def getYesterday(std: Boolean = true): String = {
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
  private[utils] def getMonth(date: Date, std: Boolean = true, isEnd: Boolean = false): String = {
    val cal = Calendar.getInstance()
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

}



trait VariableType {

  def getValue: String
  def calculator(signal: String, bValue: String): String

}

case class DateType(value: CustomDateType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = signal match {
    case "+" => value + bValue.toInt
    case "-" => value - bValue.toInt
    case _ => throw new LinkisCommonErrorException(20046,s"Date class is not supported to uss：$signal.")
  }
}

case class LongType(value: Long) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = signal match {
    case "+" => val res = value + bValue.toLong; res.toString
    case "-" => val res = value - bValue.toLong; res.toString
    case "*" => val res = value * bValue.toLong; res.toString
    case "/" => val res = value / bValue.toLong; res.toString
    case _ => throw new LinkisCommonErrorException(20047,s"Int class is not supported to uss：$signal.")
  }
}

case class DoubleValue(value: Double) extends VariableType {
  override def getValue: String = doubleOrLong(value).toString

  def calculator(signal: String, bValue: String): String = signal match {
    case "+" => val res = value + bValue.toDouble; doubleOrLong(res).toString
    case "-" => val res = value - bValue.toDouble; doubleOrLong(res).toString
    case "*" => val res = value * bValue.toDouble; doubleOrLong(res).toString
    case "/" => val res = value / bValue.toDouble; doubleOrLong(res).toString
    case _ => throw new LinkisCommonErrorException(20047,s"Double class is not supported to uss：$signal.")
  }

  private def doubleOrLong(d:Double):AnyVal = {
    if (d.asInstanceOf[Long] == d) d.asInstanceOf[Long] else d
  }

}

case class FloatType(value: Float) extends VariableType {
  override def getValue: String = floatOrLong(value).toString

  def calculator(signal: String, bValue: String): String = signal match {
    case "+" => val res = value + bValue.toFloat; floatOrLong(res).toString
    case "-" => val res = value - bValue.toFloat; floatOrLong(res).toString
    case "*" => val res = value * bValue.toFloat; floatOrLong(res).toString
    case "/" => val res = value / bValue.toLong; floatOrLong(res).toString
    case _ => throw new LinkisCommonErrorException(20048,s"Float class is not supported to use：$signal.")
  }

  private def floatOrLong(f:Float):AnyVal = {
    if (f.asInstanceOf[Long] == f) f.asInstanceOf[Long] else f
  }

}

case class StringType(value: String) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = signal match {
    case "+" => value + bValue
    case _ => throw new LinkisCommonErrorException(20049,s"String class is not supported to uss：$signal.")
  }
}

import VariableUtils._
class CustomDateType(date: String, std: Boolean = true) {

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

class CustomMonthType(date: String, std: Boolean = true, isEnd: Boolean = false)
  extends CustomDateType(date, std) {

  override def -(months: Int): String = if (std) {
    getMonth(DateUtils.addMonths(dateFormat_std.parse(date), -months), std, isEnd)
  } else {
    getMonth(DateUtils.addMonths(dateFormat.parse(date), -months), std, isEnd)
  }

  override def +(months: Int): String = if (std) {
    getMonth(DateUtils.addMonths(dateFormat_std.parse(date), months), std, isEnd)
  } else {
    getMonth(DateUtils.addMonths(dateFormat.parse(date), months), std, isEnd)
  }

  override def toString: String = {
    if (std) {
      VariableUtils.getMonth(dateFormat_std.parse(date), std, isEnd)
    } else {
      val v = dateFormat.parse(date)
      VariableUtils.getMonth(v, std, isEnd)
    }
  }

}