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

package org.apache.linkis.rpc.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import scala.Option;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializeUtil {

  private static final Delegate<Option> TIMESTAMP_DELEGATE = new NoneDelegate();

  private static final DefaultIdStrategy idStrategy = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);

  static {
    idStrategy.registerDelegate(TIMESTAMP_DELEGATE);
  }

  private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

  public static <T> String serialize(T obj) {

    if (obj == null) {
      throw new NullPointerException();
    }
    Class<T> clazz = (Class<T>) obj.getClass();
    Schema<T> schema = getSchema(clazz);
    byte[] data;
    LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    try {
      data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
    } finally {
      buffer.clear();
    }
    return toHexString(data);
  }

  public static <T> T deserialize(String str, Class<T> clazz) {
    Schema<T> schema = getSchema(clazz);
    T obj = schema.newMessage();
    ProtostuffIOUtil.mergeFrom(toByteArray(str), obj, schema);
    return obj;
  }

  private static <T> Schema<T> getSchema(Class<T> clazz) {
    Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
    if (schema == null) {
      schema = RuntimeSchema.createFrom(clazz, idStrategy);
      if (schema != null) {
        schemaCache.put(clazz, schema);
      }
    }

    return schema;
  }

  /**
   * @param hexString
   * @return
   */
  public static byte[] toByteArray(String hexString) {
    if (null == hexString) throw new IllegalArgumentException("this hexString must not be null");

    if ("".equals(hexString)) return new byte[0];

    hexString = hexString.toLowerCase();
    final byte[] byteArray = new byte[hexString.length() / 2];
    int k = 0;
    for (int i = 0; i < byteArray.length; i++) {
      byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
      byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
      byteArray[i] = (byte) (high << 4 | low);
      k += 2;
    }
    return byteArray;
  }

  public static String toHexString(byte[] byteArray) {
    if (byteArray == null) throw new IllegalArgumentException("this byteArray must not be null ");

    final StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < byteArray.length; i++) {
      if ((byteArray[i] & 0xff) < 0x10) // 0~F前面不零
      hexString.append("0");
      hexString.append(Integer.toHexString(0xFF & byteArray[i]));
    }
    return hexString.toString().toLowerCase();
  }
}
