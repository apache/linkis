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

package org.apache.linkis.engineplugin.hive.serde;

import org.apache.linkis.common.utils.ClassUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hive.serde2.ByteStream;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.io.HiveCharWritable;
import org.apache.hadoop.hive.serde2.io.HiveVarcharWritable;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.lazy.LazyUtils;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.*;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerDelimitedJSONSerDe extends LazySimpleSerDe {
  public static final Logger LOG =
      LoggerFactory.getLogger(CustomerDelimitedJSONSerDe.class.getName());
  public static byte[] trueBytes = {(byte) 't', 'r', 'u', 'e'};
  public static byte[] falseBytes = {(byte) 'f', 'a', 'l', 's', 'e'};

  public CustomerDelimitedJSONSerDe() throws SerDeException {}

  /** Not implemented. */
  @Override
  public Object doDeserialize(Writable field) throws SerDeException {
    LOG.error("DelimitedJSONSerDe cannot deserialize.");
    throw new SerDeException("DelimitedJSONSerDe cannot deserialize.");
  }

  public static void serialize(
      ByteStream.Output out,
      Object obj,
      ObjectInspector objInspector,
      byte[] separators,
      int level,
      Text nullSequence,
      boolean escaped,
      byte escapeChar,
      boolean[] needsEscape)
      throws IOException, SerDeException {
    if (obj == null) {
      out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
    } else {
      char separator;
      List list;
      switch (objInspector.getCategory()) {
        case PRIMITIVE:
          writePrimitiveUTF8(
              out, obj, (PrimitiveObjectInspector) objInspector, escaped, escapeChar, needsEscape);
          return;
        case LIST:
          separator = (char) getSeparator(separators, level);
          ListObjectInspector loi = (ListObjectInspector) objInspector;
          list = loi.getList(obj);
          ObjectInspector eoi = loi.getListElementObjectInspector();
          if (list == null) {
            out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
          } else {
            for (int i = 0; i < list.size(); ++i) {
              if (i > 0) {
                out.write(separator);
              }

              serialize(
                  out,
                  list.get(i),
                  eoi,
                  separators,
                  level + 1,
                  nullSequence,
                  escaped,
                  escapeChar,
                  needsEscape);
            }
          }

          return;
        case MAP:
          separator = (char) getSeparator(separators, level);
          char keyValueSeparator = (char) getSeparator(separators, level + 1);
          MapObjectInspector moi = (MapObjectInspector) objInspector;
          ObjectInspector koi = moi.getMapKeyObjectInspector();
          ObjectInspector voi = moi.getMapValueObjectInspector();
          Map<?, ?> map = moi.getMap(obj);
          if (map == null) {
            out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
          } else {
            boolean first = true;
            Iterator var24 = map.entrySet().iterator();

            while (var24.hasNext()) {
              Map.Entry<?, ?> entry = (Map.Entry) var24.next();
              if (first) {
                first = false;
              } else {
                out.write(separator);
              }

              serialize(
                  out,
                  entry.getKey(),
                  koi,
                  separators,
                  level + 2,
                  nullSequence,
                  escaped,
                  escapeChar,
                  needsEscape);
              out.write(keyValueSeparator);
              serialize(
                  out,
                  entry.getValue(),
                  voi,
                  separators,
                  level + 2,
                  nullSequence,
                  escaped,
                  escapeChar,
                  needsEscape);
            }
          }

          return;
        case STRUCT:
          separator = (char) getSeparator(separators, level);
          StructObjectInspector soi = (StructObjectInspector) objInspector;
          List<? extends StructField> fields = soi.getAllStructFieldRefs();
          list = soi.getStructFieldsDataAsList(obj);
          if (list == null) {
            out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
          } else {
            for (int i = 0; i < list.size(); ++i) {
              if (i > 0) {
                out.write(separator);
              }

              serialize(
                  out,
                  list.get(i),
                  ((StructField) fields.get(i)).getFieldObjectInspector(),
                  separators,
                  level + 1,
                  nullSequence,
                  escaped,
                  escapeChar,
                  needsEscape);
            }
          }

          return;
        case UNION:
          separator = (char) getSeparator(separators, level);
          UnionObjectInspector uoi = (UnionObjectInspector) objInspector;
          List<? extends ObjectInspector> ois = uoi.getObjectInspectors();
          if (ois == null) {
            out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
          } else {
            LazyUtils.writePrimitiveUTF8(
                out,
                new Byte(uoi.getTag(obj)),
                PrimitiveObjectInspectorFactory.javaByteObjectInspector,
                escaped,
                escapeChar,
                needsEscape);
            out.write(separator);
            serialize(
                out,
                uoi.getField(obj),
                (ObjectInspector) ois.get(uoi.getTag(obj)),
                separators,
                level + 1,
                nullSequence,
                escaped,
                escapeChar,
                needsEscape);
          }

          return;
        default:
          throw new RuntimeException("Unknown category type: " + objInspector.getCategory());
      }
    }
  }

  private static void writePrimitiveUTF8(
      OutputStream out,
      Object o,
      PrimitiveObjectInspector oi,
      boolean escaped,
      byte escapeChar,
      boolean[] needsEscape)
      throws IOException {

    PrimitiveObjectInspector.PrimitiveCategory category = oi.getPrimitiveCategory();
    byte[] binaryData = null;
    WritableComparable wc = null;
    switch (category) {
      case BOOLEAN:
        {
          boolean b = ((BooleanObjectInspector) oi).get(o);
          if (b) {
            binaryData = Base64.encodeBase64(trueBytes);
          } else {
            binaryData = Base64.encodeBase64(falseBytes);
          }
          break;
        }
      case BYTE:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((ByteObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case SHORT:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((ShortObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case INT:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((IntObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case LONG:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((LongObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case FLOAT:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((FloatObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case DOUBLE:
        {
          binaryData =
              Base64.encodeBase64(String.valueOf(((DoubleObjectInspector) oi).get(o)).getBytes());
          break;
        }
      case STRING:
        {
          binaryData =
              Base64.encodeBase64(
                  ((StringObjectInspector) oi).getPrimitiveWritableObject(o).getBytes());
          break;
        }
      case CHAR:
        {
          HiveCharWritable hc = ((HiveCharObjectInspector) oi).getPrimitiveWritableObject(o);
          binaryData = Base64.encodeBase64(String.valueOf(hc).getBytes());
          break;
        }
      case VARCHAR:
        {
          HiveVarcharWritable hc = ((HiveVarcharObjectInspector) oi).getPrimitiveWritableObject(o);
          binaryData = Base64.encodeBase64(String.valueOf(hc).getBytes());
          break;
        }
      case BINARY:
        {
          BytesWritable bw = ((BinaryObjectInspector) oi).getPrimitiveWritableObject(o);
          byte[] toEncode = new byte[bw.getLength()];
          System.arraycopy(bw.getBytes(), 0, toEncode, 0, bw.getLength());
          byte[] toWrite = Base64.encodeBase64(toEncode);
          out.write(toWrite, 0, toWrite.length);
          break;
        }
      case DATE:
        {
          wc = ((DateObjectInspector) oi).getPrimitiveWritableObject(o);
          binaryData = Base64.encodeBase64(String.valueOf(wc).getBytes());
          break;
        }
      case TIMESTAMP:
        {
          wc = ((TimestampObjectInspector) oi).getPrimitiveWritableObject(o);
          binaryData = Base64.encodeBase64(String.valueOf(wc).getBytes());
          break;
        }
      case DECIMAL:
        {
          HiveDecimalObjectInspector decimalOI = (HiveDecimalObjectInspector) oi;
          binaryData = Base64.encodeBase64(String.valueOf(decimalOI).getBytes());
          break;
        }
      default:
        {
          if (!"INTERVAL_YEAR_MONTH".equals(category.name())
              && !"INTERVAL_DAY_TIME".equals(category.name())) {
            throw new RuntimeException("Unknown primitive type: " + category);
          }
          boolean containsIntervalYearMonth = false;
          boolean containsIntervalDayTime = false;
          for (PrimitiveObjectInspector.PrimitiveCategory primitiveCategory :
              PrimitiveObjectInspector.PrimitiveCategory.values()) {
            containsIntervalYearMonth =
                "INTERVAL_YEAR_MONTH".equals(primitiveCategory.name())
                    && "INTERVAL_YEAR_MONTH".equals(category.name());
            containsIntervalDayTime =
                "INTERVAL_DAY_TIME".equals(primitiveCategory.name())
                    && "INTERVAL_DAY_TIME".equals(category.name());
            try {
              if (containsIntervalYearMonth) {
                wc =
                    (WritableComparable)
                        ClassUtils.getClassInstance(
                                "org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveIntervalYearMonthObjectInspector")
                            .getClass()
                            .getMethod("getPrimitiveWritableObject", Object.class)
                            .invoke(oi, o);
                binaryData = Base64.encodeBase64(String.valueOf(wc).getBytes());
                break;
              }
              if (containsIntervalDayTime) {
                wc =
                    (WritableComparable)
                        ClassUtils.getClassInstance(
                                "org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveIntervalDayTimeObjectInspector")
                            .getClass()
                            .getMethod("getPrimitiveWritableObject", Object.class)
                            .invoke(oi, o);
                binaryData = Base64.encodeBase64(String.valueOf(wc).getBytes());
                break;
              }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
              LOG.error("Fail to invoke method:[getPrimitiveWritableObject]!", e);
            }
          }
          if (containsIntervalYearMonth || containsIntervalDayTime) {
            break;
          } else {
            throw new RuntimeException("Unknown primitive type: " + category);
          }
        }
    }
    if (binaryData == null) {
      throw new RuntimeException("get primitive type is null: " + category);
    }
    out.write(binaryData, 0, binaryData.length);
  }

  static byte getSeparator(byte[] separators, int level) throws SerDeException {
    try {
      return separators[level];
    } catch (ArrayIndexOutOfBoundsException var5) {
      String msg =
          "Number of levels of nesting supported for LazySimpleSerde is "
              + (separators.length - 1)
              + " Unable to work with level "
              + level;
      String txt = ". Use %s serde property for tables using LazySimpleSerde.";
      if (separators.length < 9) {
        msg = msg + String.format(txt, "hive.serialization.extend.nesting.levels");
      } else if (separators.length < 25) {
        msg = msg + String.format(txt, "hive.serialization.extend.additional.nesting.levels");
      }

      throw new SerDeException(msg, var5);
    }
  }
}
