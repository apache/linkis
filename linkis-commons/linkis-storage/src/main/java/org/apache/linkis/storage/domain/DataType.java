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

package org.apache.linkis.storage.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DataType {
  NullType("void", 0),
  StringType("string", 12),
  BooleanType("boolean", 16),
  TinyIntType("tinyint", -6),
  ShortIntType("short", 5),
  IntType("int", 4),
  LongType("long", -5),
  BigIntType("bigint", -5),
  FloatType("float", 6),
  DoubleType("double", 8),
  CharType("char", 1),
  VarcharType("varchar", 12),
  DateType("date", 91),
  TimestampType("timestamp", 93),
  BinaryType("binary", -2),
  DecimalType("decimal", 3),
  ArrayType("array", 2003),
  MapType("map", 2000),
  ListType("list", 2001),
  StructType("struct", 2002),
  BigDecimalType("bigdecimal", 3);

  private final String typeName;
  private final int javaSQLType;

  DataType(String typeName, int javaSQLType) {
    this.typeName = typeName;
    this.javaSQLType = javaSQLType;
  }

  private static Logger logger = LoggerFactory.getLogger(DataType.class);

  public static final String NULL_VALUE = "NULL";
  public static final String LOWCASE_NULL_VALUE = "null";

  // TODO Change to fine-grained regular expressions(改为精细化正则表达式)
  public static final Pattern DECIMAL_REGEX = Pattern.compile("^decimal\\(\\d*\\,\\d*\\)");

  public static final Pattern SHORT_REGEX = Pattern.compile("^short.*");
  public static final Pattern INT_REGEX = Pattern.compile("^int.*");
  public static final Pattern LONG_REGEX = Pattern.compile("^long.*");
  public static final Pattern BIGINT_REGEX = Pattern.compile("^bigint.*");
  public static final Pattern FLOAT_REGEX = Pattern.compile("^float.*");
  public static final Pattern DOUBLE_REGEX = Pattern.compile("^double.*");

  public static final Pattern VARCHAR_REGEX = Pattern.compile("^varchar.*");
  public static final Pattern CHAR_REGEX = Pattern.compile("^char.*");

  public static final Pattern ARRAY_REGEX = Pattern.compile("array.*");

  public static final Pattern MAP_REGEX = Pattern.compile("map.*");

  public static final Pattern LIST_REGEX = Pattern.compile("list.*");

  public static final Pattern STRUCT_REGEX = Pattern.compile("struct.*");

  public static DataType toDataType(String dataType) {
    if (dataType.equals("void") || dataType.equals("null")) {
      return DataType.NullType;
    } else if (dataType.equals("string")) {
      return DataType.StringType;
    } else if (dataType.equals("boolean")) {
      return DataType.BooleanType;
    } else if (SHORT_REGEX.matcher(dataType).matches()) {
      return DataType.ShortIntType;
    } else if (LONG_REGEX.matcher(dataType).matches()) {
      return DataType.LongType;
    } else if (BIGINT_REGEX.matcher(dataType).matches()) {
      return DataType.BigIntType;
    } else if (INT_REGEX.matcher(dataType).matches()
        || dataType.equals("integer")
        || dataType.equals("smallint")) {
      return DataType.IntType;
    } else if (FLOAT_REGEX.matcher(dataType).matches()) {
      return DataType.FloatType;
    } else if (DOUBLE_REGEX.matcher(dataType).matches()) {
      return DataType.DoubleType;
    } else if (VARCHAR_REGEX.matcher(dataType).matches()) {
      return DataType.VarcharType;
    } else if (CHAR_REGEX.matcher(dataType).matches()) {
      return DataType.CharType;
    } else if (dataType.equals("date")) {
      return DataType.DateType;
    } else if (dataType.equals("timestamp")) {
      return DataType.TimestampType;
    } else if (dataType.equals("binary")) {
      return DataType.BinaryType;
    } else if (dataType.equals("decimal") || DECIMAL_REGEX.matcher(dataType).matches()) {
      return DataType.DecimalType;
    } else if (ARRAY_REGEX.matcher(dataType).matches()) {
      return DataType.ArrayType;
    } else if (MAP_REGEX.matcher(dataType).matches()) {
      return DataType.MapType;
    } else if (LIST_REGEX.matcher(dataType).matches()) {
      return DataType.ListType;
    } else if (STRUCT_REGEX.matcher(dataType).matches()) {
      return DataType.StructType;
    } else {
      return DataType.StringType;
    }
  }

  public static Object toValue(DataType dataType, String value) {
    Object result = null;
    try {
      switch (dataType) {
        case NullType:
          result = null;
          break;
        case StringType:
        case CharType:
        case VarcharType:
        case StructType:
        case ListType:
        case ArrayType:
        case MapType:
          result = value;
          break;
        case BooleanType:
          result = isNumberNull(value) ? null : Boolean.valueOf(value);
          break;
        case ShortIntType:
          result = isNumberNull(value) ? null : Short.valueOf(value);
          break;
        case IntType:
          result = isNumberNull(value) ? null : Integer.valueOf(value);
          break;
        case LongType:
        case BigIntType:
          result = isNumberNull(value) ? null : Long.valueOf(value);
          break;
        case FloatType:
          result = isNumberNull(value) ? null : Float.valueOf(value);
          break;
        case DoubleType:
          result = isNumberNull(value) ? null : Double.valueOf(value);
          break;
        case DecimalType:
          result = isNumberNull(value) ? null : new BigDecimal(value);
          break;
        case DateType:
          result = isNumberNull(value) ? null : Date.valueOf(value);
          break;
        case TimestampType:
          result =
              isNumberNull(value)
                  ? null
                  : Optional.of(value)
                      .map(Timestamp::valueOf)
                      .map(Timestamp::toString)
                      .map(s -> s.endsWith(".0") ? s.substring(0, s.length() - 2) : s)
                      .orElse(null);
          break;
        case BinaryType:
          result = isNull(value) ? null : value.getBytes();
          break;
        default:
          result = value;
      }
    } catch (Exception e) {
      logger.debug("Failed to " + value + " switch to dataType:", e);
      result = value;
    }
    return result;
  }

  public static boolean isNull(String value) {
    return value == null || value.equals(NULL_VALUE) || value.trim().equals("");
  }

  public static boolean isNumberNull(String value) {
    return value == null || value.equalsIgnoreCase(NULL_VALUE) || value.trim().equals("");
  }

  public static String valueToString(Object value) {
    if (value == null) {
      return LOWCASE_NULL_VALUE;
    } else if (value instanceof BigDecimal) {
      return ((BigDecimal) value).toPlainString();
    } else {
      return value.toString();
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public int getJavaSQLType() {
    return javaSQLType;
  }

  @Override
  public String toString() {
    return typeName;
  }
}
