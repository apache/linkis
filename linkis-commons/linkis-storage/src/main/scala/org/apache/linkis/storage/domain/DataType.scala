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

package org.apache.linkis.storage.domain

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.conf.LinkisStorageConf

import java.math.{BigDecimal => JavaBigDecimal}
import java.sql.{Date, Timestamp}

object DataType extends Logging {

  val LOWCASE_NULL_VALUE = "null"

  val DECIMAL_REGEX = "^decimal\\(\\d*\\,\\d*\\)".r.unanchored
  val DECIMAL_REGEX_BLANK = "^decimal\\(\\s*\\d*\\s*,\\s*\\d*\\s*\\)".r.unanchored

  val SHORT_REGEX = "^short.*".r.unanchored
  val INT_REGEX = "^int.*".r.unanchored
  val LONG_REGEX = "^long.*".r.unanchored
  val BIGINT_REGEX = "^bigint.*".r.unanchored
  val FLOAT_REGEX = "^float.*".r.unanchored
  val DOUBLE_REGEX = "^double.*".r.unanchored

  val VARCHAR_REGEX = "^varchar.*".r.unanchored
  val CHAR_REGEX = "^char.*".r.unanchored

  val ARRAY_REGEX = "array.*".r.unanchored

  val MAP_REGEX = "map.*".r.unanchored

  val LIST_REGEX = "list.*".r.unanchored

  val STRUCT_REGEX = "struct.*".r.unanchored

  implicit def toDataType(dataType: String): DataType = dataType match {
    case "void" | "null" => NullType
    case "string" => StringType
    case "boolean" => BooleanType
    case SHORT_REGEX() => ShortIntType
    case LONG_REGEX() => LongType
    case BIGINT_REGEX() => BigIntType
    case INT_REGEX() | "integer" | "smallint" => IntType
    case FLOAT_REGEX() => FloatType
    case DOUBLE_REGEX() => DoubleType
    case VARCHAR_REGEX() => VarcharType
    case CHAR_REGEX() => CharType
    case "date" => DateType
    case "timestamp" => TimestampType
    case "binary" => BinaryType
    case "decimal" | DECIMAL_REGEX() | DECIMAL_REGEX_BLANK() => DecimalType
    case ARRAY_REGEX() => ArrayType
    case MAP_REGEX() => MapType
    case LIST_REGEX() => ListType
    case STRUCT_REGEX() => StructType
    case _ => StringType
  }

  def toValue(dataType: DataType, value: String): Any = {
    var newValue: String = value
    if (isLinkisNull(value)) {
      if (!LinkisStorageConf.LINKIS_RESULT_ENABLE_NULL) {
        return null
      } else {
        newValue = Dolphin.NULL
      }
    }
    Utils.tryCatch(dataType match {
      case NullType => null
      case StringType | CharType | VarcharType | StructType | ListType | ArrayType | MapType =>
        newValue
      case BooleanType => if (isNumberNull(newValue)) null else newValue.toBoolean
      case ShortIntType => if (isNumberNull(newValue)) null else newValue.toShort
      case IntType => if (isNumberNull(newValue)) null else newValue.toInt
      case LongType | BigIntType => if (isNumberNull(newValue)) null else newValue.toLong
      case FloatType => if (isNumberNull(newValue)) null else newValue.toFloat
      case DoubleType => if (isNumberNull(newValue)) null else newValue.toDouble
      case DecimalType => if (isNumberNull(newValue)) null else new JavaBigDecimal(newValue)
      case DateType => if (isNumberNull(newValue)) null else Date.valueOf(newValue)
      case TimestampType =>
        if (isNumberNull(newValue)) null else Timestamp.valueOf(newValue).toString.stripSuffix(".0")
      case BinaryType => if (isNull(newValue)) null else newValue.getBytes()
      case _ => newValue
    }) { t =>
      logger.debug(s"Failed to  $newValue switch  to dataType:", t)
      newValue
    }
  }

  def isLinkisNull(value: String): Boolean = {
    if (value == null || value == Dolphin.LINKIS_NULL) true else false
  }

  def isNull(value: String): Boolean =
    if (value == null || value == Dolphin.NULL || value.trim == "") true else false

  def isNumberNull(value: String): Boolean =
    if (null == value || Dolphin.NULL.equalsIgnoreCase(value) || value.trim == "") {
      true
    } else {
      false
    }

  def valueToString(value: Any): String = {
    if (null == value) return null
    value match {
      case javaDecimal: JavaBigDecimal =>
        javaDecimal.toPlainString
      case _ => value.toString
    }
  }

}

abstract class DataType(val typeName: String, val javaSQLType: Int) {
  override def toString: String = typeName
}

case object NullType extends DataType("void", 0)
case object StringType extends DataType("string", 12)
case object BooleanType extends DataType("boolean", 16)
case object TinyIntType extends DataType("tinyint", -6)
case object ShortIntType extends DataType("short", 5)
case object IntType extends DataType("int", 4)
case object LongType extends DataType("long", -5)
case object BigIntType extends DataType("bigint", -5)
case object FloatType extends DataType("float", 6)
case object DoubleType extends DataType("double", 8)
case object CharType extends DataType("char", 1)
case object VarcharType extends DataType("varchar", 12)
case object DateType extends DataType("date", 91)
case object TimestampType extends DataType("timestamp", 93)
case object BinaryType extends DataType("binary", -2)
case object DecimalType extends DataType("decimal", 3)
case object ArrayType extends DataType("array", 2003)
case object MapType extends DataType("map", 2000)
case object ListType extends DataType("list", 2001)
case object StructType extends DataType("struct", 2002)
case object BigDecimalType extends DataType("bigdecimal", 3)

case class Column(columnName: String, dataType: DataType, comment: String) {

  def toArray: Array[Any] = {
    Array[Any](columnName, dataType, comment)
  }

  override def toString: String = s"columnName:$columnName,dataType:$dataType,comment:$comment"
}
