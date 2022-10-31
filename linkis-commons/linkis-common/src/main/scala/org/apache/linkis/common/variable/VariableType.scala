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

import org.apache.linkis.common.exception.LinkisCommonErrorException

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
      case _ =>
        throw new LinkisCommonErrorException(20046, s"DateType is not supported to use:$signal")
    }
  }

}

case class MonthType(value: CustomMonthType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"MonthType is not supported to use:$signal")
    }
  }

}

case class MonType(value: CustomMonType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"MonType is not supported to use:$signal")
    }
  }

}

case class QuarterType(value: CustomQuarterType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"QuarterType is not supported to use:$signal")
    }
  }

}

case class HalfYearType(value: CustomHalfYearType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"HalfYearType is not supported to use:$signal")
    }
  }

}

case class YearType(value: CustomYearType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"YearType is not supported to use:$signal")
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
      case _ =>
        throw new LinkisCommonErrorException(20047, s"LongType is not supported to use:$signal")
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
      case _ =>
        throw new LinkisCommonErrorException(20047, s"Double class is not supported to use:$signal")
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
      case _ =>
        throw new LinkisCommonErrorException(20048, s"Float class is not supported to use:$signal")
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
      case _ =>
        throw new LinkisCommonErrorException(20049, s"String class is not supported to use:$signal")
    }
  }

}

case class HourType(value: CustomHourType) extends VariableType {
  override def getValue: String = value.toString

  def calculator(signal: String, bValue: String): String = {
    signal match {
      case "+" => value + bValue.toInt
      case "-" => value - bValue.toInt
      case _ =>
        throw new LinkisCommonErrorException(20046, s"HourType is not supported to use:$signal")
    }
  }

}
