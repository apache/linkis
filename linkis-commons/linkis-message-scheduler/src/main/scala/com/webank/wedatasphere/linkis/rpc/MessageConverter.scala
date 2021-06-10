/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.rpc

import java.lang.reflect.Modifier
import java.util

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.message.annotation.Method
import com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.{REFLECTIONS, _}
import com.webank.wedatasphere.linkis.message.exception.MessageErrorException
import com.webank.wedatasphere.linkis.protocol.message.RequestMethod
import com.webank.wedatasphere.linkis.rpc.exception.DWCURIException
import com.webank.wedatasphere.linkis.server.{BDPJettyServerHelper, Message}

import scala.collection.JavaConversions._


class MessageConverter {

  private val protocolNameCache = new util.HashMap[String, String]

  REFLECTIONS.getTypesAnnotatedWith(classOf[Method]).foreach { t =>
    val method = t.getAnnotation(classOf[Method])
    protocolNameCache.put(method.value(), t.getName)
  }

  REFLECTIONS.getSubTypesOf(classOf[RequestMethod]).filter(!_.isInterface).filter(c => !Modifier.isAbstract(c.getModifiers)).foreach { t =>
    val protocol = try {
      t.newInstance()
    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"Failed to create new instance of class ${t.getName}", e)
    }
    val method = t.getMethod("method").invoke(protocol).toString
    protocolNameCache.put(method, t.getName)
  }

  @throws[MessageErrorException]
  def convert(message: Message): util.Map[String, Object] = {
    val methodUrl = message.getMethod
    val protocolStr = protocolNameCache.get(methodUrl)
    if (protocolStr == null) throw new MessageErrorException(MessageErrorConstants.MESSAGE_ERROR, s"no " +
      s"suitable " +
      s"protocol was found" +
      s" for method:${methodUrl}")
    val returnType = new util.HashMap[String, Object]()
    val data = message.getData
    returnType += REQUEST_KEY -> data.remove(REQUEST_KEY)
    val protocol = Utils.tryThrow(Class.forName(protocolStr)) {
      case _: ClassNotFoundException =>
        new DWCURIException(10003, s"The corresponding anti-sequence class $protocolStr was not found.(找不到对应的反序列类$protocolStr.)")
      case t: ExceptionInInitializerError =>
        val exception = new DWCURIException(10004, s"The corresponding anti-sequence class ${protocolStr} failed to initialize.(对应的反序列类${protocolStr}初始化失败.)")
        exception.initCause(t)
        exception
      case t: Throwable => t
    }
    returnType += "_request_protocol_" -> BDPJettyServerHelper.gson.fromJson(BDPJettyServerHelper.gson.toJson(data), protocol)
    //设置一个restful请求的客户端
    // TODO:  req中获取到ip和地址
    data.clear()
    data.put("name", "")
    data.put("instance", "")
    returnType
  }

}
