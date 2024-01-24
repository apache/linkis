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

package org.apache.linkis.storage.utils;

import org.apache.linkis.storage.domain.DataType;

import org.apache.orc.TypeDescription;
import org.apache.orc.storage.common.type.HiveDecimal;
import org.apache.orc.storage.ql.exec.vector.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Inspired by:
 * https://github.com/apache/flink/blob/master/flink-formats/flink-orc/src/main/java/org/apache/flink/orc/OrcSplitReaderUtil.java
 */
public class OrcUtils {

  public static TypeDescription dataTypeToOrcType(DataType type) {
    switch (type) {
      case CharType:
        return TypeDescription.createChar().withMaxLength(1024);
      case StringType:
        return TypeDescription.createString();
      case LongType:
        return TypeDescription.createLong();
      case VarcharType:
        return TypeDescription.createVarchar().withMaxLength(1024);
      case BooleanType:
        return TypeDescription.createBoolean();
      case BinaryType:
        return TypeDescription.createBinary();
      case DecimalType:
        return TypeDescription.createDecimal().withScale(10).withPrecision(38);
      case TinyIntType:
        return TypeDescription.createByte();
      case ShortIntType:
        return TypeDescription.createShort();
      case IntType:
        return TypeDescription.createInt();
      case BigIntType:
        return TypeDescription.createLong();
      case FloatType:
        return TypeDescription.createFloat();
      case DoubleType:
        return TypeDescription.createDouble();
      case DateType:
        return TypeDescription.createDate();
      case TimestampType:
        return TypeDescription.createTimestamp();
      case ArrayType:
        return TypeDescription.createList(dataTypeToOrcType(DataType.VarcharType));
      case MapType:
        return TypeDescription.createMap(
            dataTypeToOrcType(DataType.VarcharType), dataTypeToOrcType(DataType.VarcharType));
      default:
        throw new UnsupportedOperationException("Unsupported type: " + type);
    }
  }

  public static void setColumn(int columnId, ColumnVector column, DataType type, Object value) {
    switch (type) {
      case CharType:
      case VarcharType:
      case BinaryType:
      case StringType:
        {
          BytesColumnVector vector = (BytesColumnVector) column;
          vector.setVal(columnId, String.valueOf(value).getBytes());
          break;
        }
      case BooleanType:
        {
          LongColumnVector vector = (LongColumnVector) column;
          vector.vector[columnId] = Boolean.valueOf(value.toString()) ? 1 : 0;
          break;
        }
      case DecimalType:
        {
          DecimalColumnVector vector = (DecimalColumnVector) column;
          vector.set(columnId, HiveDecimal.create(new BigDecimal(value.toString())));
          break;
        }
      case TinyIntType:
        {
          LongColumnVector vector = (LongColumnVector) column;
          vector.vector[columnId] = (byte) value;
          break;
        }
      case DateType:
      case IntType:
        {
          LongColumnVector vector = (LongColumnVector) column;
          vector.vector[columnId] = Integer.valueOf(value.toString());
          break;
        }
      case BigIntType:
        {
          LongColumnVector vector = (LongColumnVector) column;
          vector.vector[columnId] = Long.valueOf(value.toString());
          break;
        }
      case FloatType:
        {
          DoubleColumnVector vector = (DoubleColumnVector) column;
          vector.vector[columnId] = Float.valueOf(value.toString());
          break;
        }
      case DoubleType:
        {
          DoubleColumnVector vector = (DoubleColumnVector) column;
          vector.vector[columnId] = Double.valueOf(value.toString());
          break;
        }
      case TimestampType:
        {
          TimestampColumnVector vector = (TimestampColumnVector) column;
          try {
            vector.set(
                columnId,
                new Timestamp(
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .parse(value.toString())
                        .getTime()));
          } catch (ParseException e) {
            vector.set(columnId, new Timestamp(System.currentTimeMillis()));
          }
          break;
        }
      default:
        throw new UnsupportedOperationException("Unsupported type: " + type);
    }
  }
}
