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

package org.apache.linkis.common.variable

import org.apache.commons.lang3.time.DateUtils

import java.util.Date

class CustomDateType(date: String, std: Boolean = true) {

  def -(days: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val dateFormat_std = DateTypeUtils.dateFormatStdLocal.get()
    if (std) {
      dateFormat_std.format(DateUtils.addDays(dateFormat_std.parse(date), -days))
    } else {
      dateFormat.format(DateUtils.addDays(dateFormat.parse(date), -days))
    }
  }

  def +(days: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val dateFormat_std = DateTypeUtils.dateFormatStdLocal.get()
    if (std) {
      dateFormat_std.format(DateUtils.addDays(dateFormat_std.parse(date), days))
    } else {
      dateFormat.format(DateUtils.addDays(dateFormat.parse(date), days))
    }
  }

  def getDate: Date = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val dateFormat_std = DateTypeUtils.dateFormatStdLocal.get()
    if (std) {
      dateFormat_std.parse(date)
    } else {
      dateFormat.parse(date)
    }
  }

  def getStdDate: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val dateFormat_std = DateTypeUtils.dateFormatStdLocal.get()
    if (std) {
      dateFormat_std.format(dateFormat_std.parse(date))
    } else {
      dateFormat_std.format(dateFormat.parse(date))
    }
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val dateFormat_std = DateTypeUtils.dateFormatStdLocal.get()
    if (std) {
      dateFormat_std.format(dateFormat_std.parse(date))
    } else {
      dateFormat.format(dateFormat.parse(date))
    }
  }

}

class CustomMonthType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  def -(months: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    } else {
      DateTypeUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    }
  }

  def +(months: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    } else {
      DateTypeUtils.getMonth(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    }
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getMonth(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      DateTypeUtils.getMonth(std, isEnd, v)
    }
  }

}

class CustomMonType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  def -(months: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatMonLocal.get()
    if (std) {
      DateTypeUtils.getMon(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    } else {
      DateTypeUtils.getMon(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), -months))
    }
  }

  def +(months: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatMonLocal.get()
    if (std) {
      DateTypeUtils.getMon(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    } else {
      DateTypeUtils.getMon(std, isEnd, DateUtils.addMonths(dateFormat.parse(date), months))
    }
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatMonLocal.get()
    if (std) {
      DateTypeUtils.getMon(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      DateTypeUtils.getMon(std, isEnd, v)
    }
  }

}

/*
 Given a Date, convert into Quarter
 */
class CustomQuarterType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  def getCurrentQuarter(date: String): Date = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    dateFormat.parse(DateTypeUtils.getQuarter(false, false, dateFormat.parse(date)))
  }

  def -(quarters: Int): String = {
    DateTypeUtils.getQuarter(
      std,
      isEnd,
      DateUtils.addMonths(getCurrentQuarter(date), -quarters * 3)
    )
  }

  def +(quarters: Int): String = {
    DateTypeUtils.getQuarter(std, isEnd, DateUtils.addMonths(getCurrentQuarter(date), quarters * 3))
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getQuarter(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      DateTypeUtils.getQuarter(std, isEnd, v)
    }
  }

}

/*
 Given a Date, convert into HalfYear
 */
class CustomHalfYearType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  def getCurrentHalfYear(date: String): Date = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    dateFormat.parse(DateTypeUtils.getHalfYear(false, false, dateFormat.parse(date)))
  }

  def -(halfYears: Int): String = {
    DateTypeUtils.getHalfYear(
      std,
      isEnd,
      DateUtils.addMonths(getCurrentHalfYear(date), -halfYears * 6)
    )
  }

  def +(halfYears: Int): String = {
    DateTypeUtils.getHalfYear(
      std,
      isEnd,
      DateUtils.addMonths(getCurrentHalfYear(date), halfYears * 6)
    )
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getHalfYear(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      DateTypeUtils.getHalfYear(std, isEnd, v)
    }
  }

}

/*
 Given a Date convert into Year
 */
class CustomYearType(date: String, std: Boolean = true, isEnd: Boolean = false) {

  def -(years: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    DateTypeUtils.getYear(std, isEnd, DateUtils.addYears(dateFormat.parse(date), -years))
  }

  def +(years: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    DateTypeUtils.getYear(std, isEnd, DateUtils.addYears(dateFormat.parse(date), years))
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    if (std) {
      DateTypeUtils.getYear(std, isEnd, dateFormat.parse(date))
    } else {
      val v = dateFormat.parse(date)
      DateTypeUtils.getYear(std, isEnd, v)
    }
  }

}

class CustomHourType(dateH: String, std: Boolean = true) {

  def -(hour: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatHourLocal.get()
    val dateFormatStd = DateTypeUtils.dateFormatHourStdLocal.get()
    if (std) {
      dateFormatStd.format(DateUtils.addHours(dateFormat.parse(dateH), -hour))
    } else {
      dateFormat.format(DateUtils.addHours(dateFormat.parse(dateH), -hour))
    }
  }

  def +(hour: Int): String = {
    val dateFormat = DateTypeUtils.dateFormatHourLocal.get()
    val dateFormatStd = DateTypeUtils.dateFormatHourStdLocal.get()
    if (std) {
      dateFormatStd.format(DateUtils.addHours(dateFormat.parse(dateH), hour))
    } else {
      dateFormat.format(DateUtils.addHours(dateFormat.parse(dateH), hour))
    }
  }

  override def toString: String = {
    val dateFormat = DateTypeUtils.dateFormatHourLocal.get()
    val dateFormatStd = DateTypeUtils.dateFormatHourStdLocal.get()
    if (std) {
      dateFormatStd.format(dateFormat.parse(dateH))
    } else {
      dateH
    }
  }

}
