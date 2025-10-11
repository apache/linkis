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

import java.lang.reflect.Type

import com.google.gson.{GsonBuilder, ToNumberPolicy}

/**
 * @param id
 *   Engine unique Id(engine唯一的Id)
 * @param fsType
 *   Fs type(fs类型)
 * @param creatorUser
 *   Create a user to start the corresponding jvm user(创建用户为对应启动的jvm用户)
 * @param proxyUser
 *   Proxy user(代理用户)
 * @param clientIp
 *   client Ip for whitelist control(ip用于白名单控制)
 * @param methodName
 *   Method name called(调用的方法名)
 * @param params
 *   Method parameter(方法参数)
 */
case class MethodEntity(
    id: Long,
    fsType: String,
    creatorUser: String,
    proxyUser: String,
    clientIp: String,
    methodName: String,
    params: Array[AnyRef]
) {

  override def toString: String = {
    s"id:$id, methodName:$methodName, fsType:$fsType, " +
      s"creatorUser:$creatorUser, proxyUser:$proxyUser, clientIp:$clientIp, "
  }

}

object MethodEntitySerializer {

  val gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
    .create

  /**
   * Serialized to code as a MethodEntity object 序列化为code为MethodEntity对象
   * @param code
   * @return
   */
  def deserializer(code: String): MethodEntity = gson.fromJson(code, classOf[MethodEntity])

  /**
   * Serialize MethodEntity to code 序列化MethodEntity为code
   * @param methodEntity
   * @return
   */
  def serializer(methodEntity: MethodEntity): String = gson.toJson(methodEntity)

  /**
   * Serialize a java object as a string 序列化java对象为字符串
   * @param value
   * @return
   */
  def serializerJavaObject(value: Any): String = gson.toJson(value)

  /**
   * Deserialize a string into a java object 将字符串解序列化为java对象
   * @param json
   * @param classType
   * @tparam T
   * @return
   */
  def deserializerToJavaObject[T](json: String, classType: Class[T]): T = {
    gson.fromJson(json, classType)
  }

  def deserializerToJavaObject[T](json: String, oType: Type): T = {
    gson.fromJson(json, oType)
  }

}
