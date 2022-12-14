/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.common.utils

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.exception.LinkisCommonErrorException
import org.apache.linkis.common.variable
import org.apache.linkis.common.variable._
import org.apache.linkis.common.variable.DateTypeUtils.{
  getCurHour,
  getMonthDay,
  getToday,
  getYesterday
}

import org.apache.commons.lang3.StringUtils

import java.time.ZonedDateTime
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.Exception.allCatch

object VariableUtils extends Logging {

  val RUN_DATE = "run_date"

  val RUN_TODAY_H = "run_today_h"

  private val codeReg =
    "\\$\\{\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*[\\+\\-\\*/]?\\s*[A-Za-z0-9_\\.]*\\s*\\}".r

  /**
   * calculation Reg Get custom variables, if it is related to the left and right variables of the
   * analytical calculation of the calculation
   */
  private val calReg =
    "(\\s*[A-Za-z][A-Za-z0-9_\\.]*\\s*)([\\+\\-\\*/]?)(\\s*[A-Za-z0-9_\\.]*\\s*)".r

  def replace(replaceStr: String): String = replace(replaceStr, new util.HashMap[String, Any](0))

  def replace(replaceStr: String, variables: util.Map[String, Any]): String = {
    val nameAndType = mutable.Map[String, variable.VariableType]()
    var run_date: CustomDateType = null
    variables.asScala.foreach {
      case (RUN_DATE, value) if !nameAndType.contains(RUN_DATE) =>
        val run_date_str = value.asInstanceOf[String]
        if (StringUtils.isNotEmpty(run_date_str)) {
          run_date = new CustomDateType(run_date_str, false)
          nameAndType(RUN_DATE) = variable.DateType(run_date)
        }
      case (key, value: String) if !nameAndType.contains(key) && StringUtils.isNotEmpty(value) =>
        nameAndType(key) =
          Utils.tryCatch[variable.VariableType](variable.DoubleValue(value.toDouble))(_ =>
            variable.StringType(value)
          )
      case _ =>
    }
    if (!nameAndType.contains(RUN_DATE) || null == run_date) {
      run_date = new CustomDateType(getYesterday(false), false)
      nameAndType(RUN_DATE) = variable.DateType(new CustomDateType(run_date.toString, false))
    }
    if (variables.containsKey(RUN_TODAY_H)) {
      val runTodayHStr = variables.get(RUN_TODAY_H).asInstanceOf[String]
      if (StringUtils.isNotBlank(runTodayHStr)) {
        val runTodayH = new CustomHourType(runTodayHStr, false)
        nameAndType(RUN_TODAY_H) = HourType(runTodayH)
      }
    }
    initAllDateVars(run_date, nameAndType)
    val codeOperation = parserVar(replaceStr, nameAndType)
    parserDate(codeOperation, run_date)
  }

  def replace(code: String, codeType: String, variables: util.Map[String, String]): String = {
    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    if (StringUtils.isBlank(languageType)) {
      return code
    }
    var run_date: CustomDateType = null

    val nameAndType = mutable.Map[String, variable.VariableType]()

    val nameAndValue: mutable.Map[String, String] = getCustomVar(code, languageType)

    def putNameAndType(data: mutable.Map[String, String]): Unit = if (null != data) data foreach {
      case (key, value) =>
        key match {
          case RUN_DATE =>
            if (!nameAndType.contains(RUN_DATE)) {
              val run_date_str = value
              if (StringUtils.isNotEmpty(run_date_str)) {
                run_date = new CustomDateType(run_date_str, false)
                nameAndType(RUN_DATE) = variable.DateType(run_date)
              }
            }
          case _ =>
            if (!nameAndType.contains(key) && StringUtils.isNotEmpty(value)) {
              if ((allCatch opt value.toDouble).isDefined) {
                nameAndType(key) = variable.DoubleValue(value.toDouble)
              } else {
                nameAndType(key) = variable.StringType(value)
              }
            }
        }
    }
    // The first step is to replace the variable from code
    putNameAndType(nameAndValue)
    if (variables.containsKey("user")) {
      nameAndType("user") = variable.StringType(variables.get("user"))
    }
    // The sceond step is to replace varibles map
    if (null != variables) {
      putNameAndType(variables.asScala)
    }
    if (!nameAndType.contains(RUN_DATE) || null == run_date) {
      run_date = new CustomDateType(getYesterday(false), false)
      nameAndType(RUN_DATE) = variable.DateType(new CustomDateType(run_date.toString, false))
    }

    if (variables.containsKey(RUN_TODAY_H)) {
      val runTodayHStr = variables.get(RUN_TODAY_H).asInstanceOf[String]
      if (StringUtils.isNotBlank(runTodayHStr)) {
        val runTodayH = new CustomHourType(runTodayHStr, false)
        nameAndType(RUN_TODAY_H) = HourType(runTodayH)
      }
    }
    initAllDateVars(run_date, nameAndType)
    val codeOperation = parserVar(code, nameAndType)
    parserDate(codeOperation, run_date)
  }

  private def parserDate(code: String, run_date: CustomDateType): String = {
    if (Configuration.VARIABLE_OPERATION) {
      val zonedDateTime: ZonedDateTime = VariableOperationUtils.toZonedDateTime(run_date.getDate)
      VariableOperationUtils.replaces(zonedDateTime, code)
    } else {
      code
    }
  }

  private def initAllDateVars(
      run_date: CustomDateType,
      nameAndType: mutable.Map[String, variable.VariableType]
  ): Unit = {
    val run_date_str = run_date.toString
    nameAndType("run_date_std") = variable.DateType(new CustomDateType(run_date.getStdDate))
    nameAndType("run_month_begin") = MonthType(new CustomMonthType(run_date_str, false))
    nameAndType("run_month_begin_std") = variable.MonthType(new CustomMonthType(run_date_str))
    nameAndType("run_month_end") =
      variable.MonthType(new CustomMonthType(run_date_str, false, true))
    nameAndType("run_month_end_std") =
      variable.MonthType(new CustomMonthType(run_date_str, true, true))

    nameAndType("run_quarter_begin") = QuarterType(new CustomQuarterType(run_date_str, false))
    nameAndType("run_quarter_begin_std") = QuarterType(new CustomQuarterType(run_date_str))
    nameAndType("run_quarter_end") = QuarterType(new CustomQuarterType(run_date_str, false, true))
    nameAndType("run_quarter_end_std") = QuarterType(
      new CustomQuarterType(run_date_str, true, true)
    )

    nameAndType("run_half_year_begin") = HalfYearType(new CustomHalfYearType(run_date_str, false))
    nameAndType("run_half_year_begin_std") = HalfYearType(new CustomHalfYearType(run_date_str))
    nameAndType("run_half_year_end") = HalfYearType(
      new CustomHalfYearType(run_date_str, false, true)
    )
    nameAndType("run_half_year_end_std") = HalfYearType(
      new CustomHalfYearType(run_date_str, true, true)
    )

    nameAndType("run_year_begin") = YearType(new CustomYearType(run_date_str, false))
    nameAndType("run_year_begin_std") = YearType(new CustomYearType(run_date_str))
    nameAndType("run_year_end") = YearType(new CustomYearType(run_date_str, false, true))
    nameAndType("run_year_end_std") = YearType(new CustomYearType(run_date_str, true, true))

    nameAndType("run_date_std") = variable.DateType(new CustomDateType(run_date.getStdDate))
    nameAndType("run_month_begin") = variable.MonthType(new CustomMonthType(run_date_str, false))
    nameAndType("run_month_begin_std") = variable.MonthType(new CustomMonthType(run_date_str))
    nameAndType("run_month_end") =
      variable.MonthType(new CustomMonthType(run_date_str, false, true))
    nameAndType("run_month_end_std") =
      variable.MonthType(new CustomMonthType(run_date_str, true, true))

    nameAndType("run_quarter_begin") = QuarterType(new CustomQuarterType(run_date_str, false))
    nameAndType("run_quarter_begin_std") = QuarterType(new CustomQuarterType(run_date_str))
    nameAndType("run_quarter_end") = QuarterType(new CustomQuarterType(run_date_str, false, true))
    nameAndType("run_quarter_end_std") = QuarterType(
      new CustomQuarterType(run_date_str, true, true)
    )

    nameAndType("run_half_year_begin") = HalfYearType(new CustomHalfYearType(run_date_str, false))
    nameAndType("run_half_year_begin_std") = HalfYearType(new CustomHalfYearType(run_date_str))
    nameAndType("run_half_year_end") = HalfYearType(
      new CustomHalfYearType(run_date_str, false, true)
    )
    nameAndType("run_half_year_end_std") = HalfYearType(
      new CustomHalfYearType(run_date_str, true, true)
    )

    nameAndType("run_year_begin") = YearType(new CustomYearType(run_date_str, false))
    nameAndType("run_year_begin_std") = YearType(new CustomYearType(run_date_str))
    nameAndType("run_year_end") = YearType(new CustomYearType(run_date_str, false, true))
    nameAndType("run_year_end_std") = YearType(new CustomYearType(run_date_str, true, true))

    /*
    calculate run_today based on run_date
     */
    val run_today = new CustomDateType(getToday(false, run_date + 1), false)
    nameAndType("run_today") = variable.DateType(new CustomDateType(run_today.toString, false))
    nameAndType("run_today_std") = variable.DateType(new CustomDateType(run_today.getStdDate))
    nameAndType("run_month_now_begin") = variable.MonthType(
      new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, false)
    )
    nameAndType("run_month_now_begin_std") =
      variable.MonthType(new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1))
    nameAndType("run_month_now_end") = variable.MonthType(
      new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, false, true)
    )
    nameAndType("run_month_now_end_std") = variable.MonthType(
      new CustomMonthType(new CustomMonthType(run_today.toString, false) - 1, true, true)
    )

    // calculate run_mon base on run_date
    val run_mon = new CustomMonType(getMonthDay(false, run_date.getDate), false)
    nameAndType("run_mon") = MonType(new CustomMonType(run_mon.toString, false))
    nameAndType("run_mon_std") = MonType(new CustomMonType(run_mon.toString, true, false))
    nameAndType("run_mon_start") = MonType(new CustomMonType(run_mon.toString, false, false))
    nameAndType("run_mon_start_std") = MonType(new CustomMonType(run_mon.toString, true, false))
    nameAndType("run_mon_end") = MonType(new CustomMonType(run_mon.toString, false, true))
    nameAndType("run_mon_end_std") = MonType(new CustomMonType(run_mon.toString, true, true))

    // calculate run_mon base on run_date
    if (nameAndType.contains(RUN_TODAY_H)) {
      nameAndType(RUN_TODAY_H).asInstanceOf[HourType]
    } else {
      val run_today_h = new CustomHourType(getCurHour(false, run_today.toString), false)
      nameAndType(RUN_TODAY_H) = HourType(run_today_h)
    }
    nameAndType("run_today_h_std") = HourType(
      new CustomHourType(nameAndType(RUN_TODAY_H).asInstanceOf[HourType].getValue, true)
    )
  }

  /**
   * Parse and replace the value of the variable
   * 1.Get the expression and calculations 2.Print user log 3.Assemble code
   *
   * @param code
   *   :code
   * @param nameAndType
   *   : variable name and Type
   * @return
   */
  def parserVar(code: String, nameAndType: mutable.Map[String, VariableType]): String = {
    val parseCode = new StringBuilder
    val codes = codeReg.split(code)
    val expressionCache = mutable.HashSet[String]()
    var i = 0
    codeReg
      .findAllIn(code)
      .foreach(str => {
        calReg
          .findFirstMatchIn(str)
          .foreach(ma => {
            i = i + 1

            /**
             * name left value rightValue right value signal: + - * /
             */
            val name = ma.group(1)
            val signal = ma.group(2)
            val rightValue = ma.group(3)

            if (name == null || name.trim.isEmpty) {
              throw new LinkisCommonErrorException(20041, s"[$str] replaced var is null")
            } else {
              var expression = name.trim
              val varType = nameAndType.get(name.trim).orNull
              if (varType == null) {
                logger.warn(
                  s"Use undefined variables or use the set method: [$str](使用了未定义的变量或者使用了set方式:[$str])"
                )
                parseCode ++= codes(i - 1) ++ str
              } else {
                var res: String = varType.getValue
                if (signal != null && !signal.trim.isEmpty) {
                  if (rightValue == null || rightValue.trim.isEmpty) {
                    throw new LinkisCommonErrorException(
                      20042,
                      s"[$str] expression is not right, please check"
                    )
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
                  expressionCache += expression
                }
                parseCode ++= codes(i - 1) ++ res
              }
            }
          })
      })
    if (i == codes.length - 1) {
      parseCode ++= codes(i)
    }
    // val parsedCode = deleteUselessSemicolon(parseCode)
    StringUtils.strip(parseCode.toString())
  }

  /**
   * Get user-defined variables and values
   *
   * @param code
   *   :code
   * @param codeType
   *   :SQL,PYTHON
   * @return
   */
  def getCustomVar(code: String, languageType: String): mutable.Map[String, String] = {
    val nameAndValue = mutable.Map[String, String]()

    var varString: String = null
    var errString: String = null

    languageType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL =>
        varString = """\s*--@set\s*.+\s*"""
        errString = """\s*--@.*"""
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_PYTHON | CodeAndRunTypeUtils.LANGUAGE_TYPE_SHELL =>
        varString = """\s*#@set\s*.+\s*"""
        errString = """\s*#@"""
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SCALA =>
        varString = """\s*//@set\s*.+\s*"""
        errString = """\s*//@.+"""
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_JAVA =>
        varString = """\s*!!@set\s*.+\s*"""
      case _ =>
        return nameAndValue
    }

    val customRegex = varString.r.unanchored
    val errRegex = errString.r.unanchored
    code.split("\n").foreach { str =>
      {
        str match {
          case customRegex() =>
            val clearStr = if (str.endsWith(";")) str.substring(0, str.length - 1) else str
            val res: Array[String] = clearStr.split("=")
            if (res != null && res.length == 2) {
              val nameSet = res(0).split("@set")
              if (nameSet != null && nameSet.length == 2) {
                val name = nameSet(1).trim
                nameAndValue(name) = res(1).trim
              }
            } else {
              if (res.length > 2) {
                throw new LinkisCommonErrorException(20044, s"$str var defined uncorrectly")
              } else {
                throw new LinkisCommonErrorException(20045, s"var was defined uncorrectly:$str")
              }
            }
          case errRegex() =>
            logger.warn(
              s"The variable definition is incorrect:$str,if it is not used, it will not run the error, but it is recommended to use the correct specification to define"
            )
          case _ =>
        }
      }
    }
    nameAndValue
  }

}
