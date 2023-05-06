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

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Serialize MethodEntity to code 序列化MethodEntity为code
 *
 * <p>Serialized to code as a MethodEntity object 序列化为code为MethodEntity对象
 *
 * <p>Serialize a java object as a string 序列化java对象为字符串
 *
 * <p>Deserialize a string into a java object 将字符串解序列化为java对象
 */
public class MethodEntitySerializer {

  private static final Gson gson =
      new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

  /**
   * Serialized to code as a MethodEntity object 序列化为code为MethodEntity对象
   *
   * @param code
   * @return
   */
  public static MethodEntity deserializer(String code) {
    return gson.fromJson(code, MethodEntity.class);
  }

  /**
   * Serialize MethodEntity to code 序列化MethodEntity为code
   *
   * @param methodEntity
   * @return
   */
  public static String serializer(MethodEntity methodEntity) {
    return gson.toJson(methodEntity);
  }

  /**
   * Serialize a java object as a string 序列化java对象为字符串
   *
   * @param value
   * @return
   */
  public static String serializerJavaObject(Object value) {
    return gson.toJson(value);
  }

  /**
   * Deserialize a string into a java object 将字符串解序列化为java对象
   *
   * @param json
   * @param classType
   * @param <T>
   * @return
   */
  public static <T> T deserializerToJavaObject(String json, Class<T> classType) {
    return gson.fromJson(json, classType);
  }

  public static <T> T deserializerToJavaObject(String json, Type oType) {
    return gson.fromJson(json, oType);
  }
}
