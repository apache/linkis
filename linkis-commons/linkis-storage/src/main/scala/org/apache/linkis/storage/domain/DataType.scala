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

import java.math.{BigDecimal => JavaBigDecimal}
import java.sql.{Date, Timestamp}

object DataType extends Logging {

  val NULL_VALUE = "NULL"
  val LOWCASE_NULL_VALUE = "null"
  // TODO Change to fine-grained regular expressions(改为精细化正则表达式)
  val DECIMAL_REGEX = "^decimal\\(\\d*\\,\\d*\\)".r.unanchored

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
    case "decimal" | DECIMAL_REGEX() => DecimalType
    case ARRAY_REGEX() => ArrayType
    case MAP_REGEX() => MapType
    case LIST_REGEX() => ListType
    case STRUCT_REGEX() => StructType
    case _ => StringType
  }

  def toValue(dataType: DataType, value: String): Any = Utils.tryCatch(dataType match {
    case NullType => null
    case StringType | CharType | VarcharType | StructType | ListType | ArrayType | MapType =>
      value
    case BooleanType => if (isNumberNull(value)) null else value.toBoolean
    case ShortIntType => if (isNumberNull(value)) null else value.toShort
    case IntType => if (isNumberNull(value)) null else value.toInt
    case LongType | BigIntType => if (isNumberNull(value)) null else value.toLong
    case FloatType => if (isNumberNull(value)) null else value.toFloat
    case DoubleType => if (isNumberNull(value)) null else value.toDouble
    case DecimalType => if (isNumberNull(value)) null else new JavaBigDecimal(value)
    case DateType => if (isNumberNull(value)) null else Date.valueOf(value)
    case TimestampType =>
      if (isNumberNull(value)) null else Timestamp.valueOf(value).toString.stripSuffix(".0")
    case BinaryType => if (isNull(value)) null else value.getBytes()
    case _ => value
  }) { t =>
    logger.debug(s"Failed to  $value switch  to dataType:", t)
    value
  }

  def isNull(value: String): Boolean =
    if (value == null || value == NULL_VALUE || value.trim == "") true else false

  def isNumberNull(value: String): Boolean =
    if (null == value || NULL_VALUE.equalsIgnoreCase(value) || value.trim == "") {
      true
    } else {
      false
    }

  def valueToString(value: Any): String = {
    if (null == value) return LOWCASE_NULL_VALUE
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
