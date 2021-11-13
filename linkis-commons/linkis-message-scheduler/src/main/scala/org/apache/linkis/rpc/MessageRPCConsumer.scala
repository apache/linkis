/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.rpc

import java.util

import org.apache.linkis.common.exception.ExceptionManager
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.rpc.exception.DWCURIException
import org.apache.linkis.rpc.serializer.ProtostuffSerializeUtil
import org.apache.linkis.rpc.transform.RPCProduct.{CLASS_VALUE, OBJECT_VALUE}
import org.apache.linkis.server.{EXCEPTION_MSG, JMap, Message}

import scala.runtime.BoxedUnit



class MessageRPCConsumer {

  private val messageConverter: MessageConverter = new MessageConverter

  def overrideToObject(message: Message): Any = {
    message.getStatus match {
      case 0 =>
        val data = message.getData
        if (data.isEmpty) return BoxedUnit.UNIT
        if (isRPCRequest(data)) {
          val objectStr = data.get(OBJECT_VALUE).toString
          val objectClass = data.get(CLASS_VALUE).toString
          val clazz = Utils.tryThrow(Class.forName(objectClass)) {
            case _: ClassNotFoundException =>
              new DWCURIException(10003, s"The corresponding anti-sequence class $objectClass was not found.(找不到对应的反序列类$objectClass.)")
            case t: ExceptionInInitializerError =>
              val exception = new DWCURIException(10004, s"The corresponding anti-sequence class ${objectClass} failed to initialize.(对应的反序列类${objectClass}初始化失败.)")
              exception.initCause(t)
              exception
            case t: Throwable => t
          }
          ProtostuffSerializeUtil.deserialize(objectStr, clazz)
        } else {
          messageConverter.convert(message)
        }
      case 4 =>
        val errorMsg = message.getData.get(EXCEPTION_MSG).asInstanceOf[JMap[String, Object]]
        ExceptionManager.generateException(errorMsg)
      case _ =>
        val errorMsg = message.getData.get(EXCEPTION_MSG)
        if (errorMsg == null) throw new DWCURIException(10005, message.getMessage)
        val realError = ExceptionManager.generateException(errorMsg.asInstanceOf[JMap[String, Object]])
        throw realError;
    }
  }

  def isRPCRequest(data: util.HashMap[String, Object]): Boolean = {
    data.containsKey(OBJECT_VALUE)
  }
}
