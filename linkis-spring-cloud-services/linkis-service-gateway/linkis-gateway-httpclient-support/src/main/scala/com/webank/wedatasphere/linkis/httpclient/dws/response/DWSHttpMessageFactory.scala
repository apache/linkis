/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.httpclient.dws.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.response.Result
import org.apache.commons.lang.ClassUtils

import scala.collection.JavaConversions._

object DWSHttpMessageFactory {

  private val reflections = com.webank.wedatasphere.linkis.common.utils.ClassUtils.reflections

  private val methodToHttpMessageClasses = reflections.getTypesAnnotatedWith(classOf[DWSHttpMessageResult])
    .filter(ClassUtils.isAssignable(_, classOf[Result])).map { c =>
    val httpMessageResult = c.getAnnotation(classOf[DWSHttpMessageResult])
    httpMessageResult.value() -> DWSHttpMessageResultInfo(httpMessageResult.value(), c)
  }.toMap
  private val methodRegex = methodToHttpMessageClasses.keys.toArray

  def getDWSHttpMessageResult(method: String): Option[DWSHttpMessageResultInfo] = methodToHttpMessageClasses.get(method).orElse {
    methodRegex.find(method.matches).map(methodToHttpMessageClasses.apply)
  }

}
case class DWSHttpMessageResultInfo(method: String, clazz: Class[_])